package com.haloalligners.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import net.coobird.thumbnailator.Thumbnails
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.UUID

@Service
class CloudinaryService(private val cloudinary: Cloudinary) {

    private val logger = LoggerFactory.getLogger(CloudinaryService::class.java)
    private val MAX_COMPRESSIBLE_IMAGE_BYTES = 2 * 1024 * 1024L // 2MB
    private val MAX_UPLOAD_BYTES = 25 * 1024 * 1024L // 25MB
    private val CHUNK_SIZE_BYTES = 6 * 1024 * 1024 // 6MB
    private val PDF_COMPRESSION_THRESHOLD_BYTES = 10 * 1024 * 1024L // 10MB
    private val PDF_MAX_IMAGE_EDGE = 1800
    private val PDF_IMAGE_QUALITY = 0.65f
    private val HEIC_EXTENSIONS = setOf("heic", "heif")
    private val HEIC_CONTENT_TYPES = setOf("image/heic", "image/heif")

    fun uploadFile(file: MultipartFile): String {
        val originalFilename = file.originalFilename?.trim().orEmpty().ifBlank { "file" }
        if (file.isEmpty || file.size == 0L) {
            throw IllegalArgumentException("Uploaded file $originalFilename is empty.")
        }
        if (file.size > MAX_UPLOAD_BYTES) {
            throw IllegalArgumentException("File $originalFilename exceeds the 25MB upload limit.")
        }

        try {
            val preparedFile = prepareFileForUpload(file)
            val uploadRequest = buildUploadRequest(preparedFile.filename)

            val params = mutableMapOf<String, Any>(
                "public_id" to uploadRequest.publicId,
                "resource_type" to uploadRequest.resourceType,
                "type" to "upload",
                "access_mode" to "public",
                "overwrite" to true
            )
            preparedFile.forcedFormat?.let { params["format"] = it }

            val fileBytes = preparedFile.bytes
            val shouldCompressImage = uploadRequest.resourceType == "image" && fileBytes.size > MAX_COMPRESSIBLE_IMAGE_BYTES
            val shouldCompressPdf = uploadRequest.resourceType == "raw" && preparedFile.filename.endsWith(".pdf", true) && fileBytes.size >= PDF_COMPRESSION_THRESHOLD_BYTES
            val uploadBytes = when {
                shouldCompressImage -> maybeCompressImage(fileBytes, uploadRequest.resourceType)
                shouldCompressPdf -> compressPdf(fileBytes)
                else -> fileBytes
            }
            val shouldUseChunkedUpload = preparedFile.forcedFormat == null && uploadBytes.size > CHUNK_SIZE_BYTES

            val uploadResult: Map<*, *> = if (shouldUseChunkedUpload) {
                params["chunk_size"] = CHUNK_SIZE_BYTES
                cloudinary.uploader().uploadLarge(uploadBytes, params)
            } else {
                cloudinary.uploader().upload(uploadBytes, params)
            }
            
            return uploadResult["secure_url"] as? String ?: throw IOException("Cloudinary upload failed: secure_url not found in response.")
        } catch (e: Exception) {
            throw RuntimeException("Could not store file $originalFilename. Please try again!", e)
        }
    }

    private fun prepareFileForUpload(file: MultipartFile): PreparedFile {
        val originalFilename = file.originalFilename?.trim().orEmpty().ifBlank { "file" }
        val fileBytes = file.bytes
        if (!isHeicFile(file, originalFilename)) {
            return PreparedFile(filename = originalFilename, bytes = fileBytes, forcedFormat = null)
        }

        val jpegFilename = originalFilename.substringBeforeLast('.', originalFilename) + ".jpg"
        return try {
            val jpegBytes = convertHeicToJpeg(fileBytes, originalFilename)
            logger.info("Converted HEIC/HEIF '{}' to JPG before Cloudinary upload.", originalFilename)
            PreparedFile(filename = jpegFilename, bytes = jpegBytes, forcedFormat = null)
        } catch (e: Exception) {
            logger.warn("Local HEIC/HEIF conversion failed for '{}'. Falling back to Cloudinary JPG output format.", originalFilename, e)
            PreparedFile(filename = originalFilename, bytes = fileBytes, forcedFormat = "jpg")
        }
    }

    private fun isHeicFile(file: MultipartFile, filename: String): Boolean {
        val extension = filename.substringAfterLast('.', "").lowercase()
        val contentType = file.contentType?.lowercase()
        return extension in HEIC_EXTENSIONS || contentType in HEIC_CONTENT_TYPES
    }

    private fun convertHeicToJpeg(fileBytes: ByteArray, filename: String): ByteArray {
        val extension = filename.substringAfterLast('.', "heic").lowercase().ifBlank { "heic" }
        val inputFile = Files.createTempFile("heic-upload-", ".${extension}")
        val outputFile = Files.createTempFile("heic-upload-", ".jpg")

        try {
            Files.write(inputFile, fileBytes)
            Files.deleteIfExists(outputFile)

            val process = ProcessBuilder(
                "sips",
                "-s", "format", "jpeg",
                inputFile.toString(),
                "--out", outputFile.toString()
            )
                .redirectErrorStream(true)
                .start()

            val commandOutput = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                throw IllegalStateException("sips failed with exit code $exitCode: $commandOutput")
            }
            if (!Files.exists(outputFile)) {
                throw IllegalStateException("sips did not create an output file.")
            }

            val jpegBytes = Files.readAllBytes(outputFile)
            if (jpegBytes.isEmpty()) {
                throw IllegalStateException("sips produced an empty JPG output.")
            }
            return jpegBytes
        } finally {
            Files.deleteIfExists(inputFile)
            Files.deleteIfExists(outputFile)
        }
    }

    private fun compressPdf(fileBytes: ByteArray): ByteArray {
        try {
            Loader.loadPDF(fileBytes).use { document ->
                recompressPdfImages(document)

                val outputStream = ByteArrayOutputStream()
                document.save(outputStream)
                val compressed = outputStream.toByteArray()
                if (compressed.isEmpty()) {
                    throw IOException("PDF compression produced an empty output.")
                }
                return compressed
            }
        } catch (e: IOException) {
            throw IllegalStateException(
                "PDF compression failed.",
                e
            )
        }
    }

    private fun recompressPdfImages(document: PDDocument) {
        for (page in document.pages) {
            val resources = page.resources ?: continue
            for (name in resources.xObjectNames) {
                val xObject = resources.getXObject(name)
                if (xObject !is PDImageXObject) {
                    continue
                }

                val sourceImage = xObject.image ?: continue
                if (sourceImage.colorModel?.hasAlpha() == true) {
                    continue
                }

                val resized = resizeIfNeeded(sourceImage, PDF_MAX_IMAGE_EDGE)
                val replacement = JPEGFactory.createFromImage(document, resized, PDF_IMAGE_QUALITY)
                resources.put(name, replacement)
            }
        }
    }

    private fun resizeIfNeeded(image: BufferedImage, maxEdge: Int): BufferedImage {
        val width = image.width
        val height = image.height
        if (width <= maxEdge && height <= maxEdge) {
            return image
        }

        val scale = minOf(maxEdge.toDouble() / width, maxEdge.toDouble() / height)
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)

        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(image)
            .size(targetWidth, targetHeight)
            .outputFormat("jpg")
            .outputQuality(PDF_IMAGE_QUALITY.toDouble())
            .toOutputStream(outputStream)

        return ByteArrayInputStream(outputStream.toByteArray()).use {
            javax.imageio.ImageIO.read(it)
        }
    }

    fun deleteFile(url: String) {
        try {
            val publicId = extractPublicIdFromUrl(url)
            val resourceType = getResourceTypeFromUrl(url)
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType))
        } catch (e: Exception) {
            logger.error("Failed to delete file from Cloudinary with URL: $url", e)
        }
    }

    fun updateFile(oldUrl: String?, newFile: MultipartFile): String {
        oldUrl?.let { deleteFile(it) }
        return uploadFile(newFile)
    }

    private fun extractPublicIdFromUrl(url: String): String {
        return url.substringAfterLast('/').substringBeforeLast('.')
    }

    private fun getResourceTypeFromUrl(url: String): String {
        val extension = url.substringAfterLast('.').lowercase()
        return when (extension) {
            "pdf" -> "raw"
            "jpg", "jpeg", "png", "gif", "heic", "heif" -> "image"
            else -> "auto"
        }
    }

    internal fun buildUploadRequest(originalFilename: String?): UploadRequest {
        val safeFilename = originalFilename?.trim().orEmpty().ifBlank { "file" }
        val extension = safeFilename.substringAfterLast('.', "").lowercase()
        val resourceType = when (extension) {
            "pdf" -> "raw"
            "jpg", "jpeg", "png", "gif", "heic", "heif" -> "image"
            else -> "auto"
        }

        val baseName = safeFilename.substringBeforeLast('.', safeFilename)
        val normalizedBaseName = normalizePublicIdSegment(baseName)
        val publicIdBase = "${normalizedBaseName}_${UUID.randomUUID().toString().substring(0, 6)}"
        val publicId = if (resourceType == "raw" && extension.isNotEmpty()) {
            "$publicIdBase.$extension"
        } else {
            publicIdBase
        }

        return UploadRequest(resourceType = resourceType, publicId = publicId)
    }

    internal fun normalizePublicIdSegment(value: String): String {
        return value
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "file" }
    }

    private fun maybeCompressImage(fileBytes: ByteArray, resourceType: String): ByteArray {
        if (resourceType != "image" || fileBytes.size <= MAX_COMPRESSIBLE_IMAGE_BYTES) {
            return fileBytes
        }

        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(ByteArrayInputStream(fileBytes))
            .size(1920, 1080)
            .outputQuality(0.8)
            .toOutputStream(outputStream)
        return outputStream.toByteArray()
    }

    internal data class UploadRequest(
        val resourceType: String,
        val publicId: String
    )

    private data class PreparedFile(
        val filename: String,
        val bytes: ByteArray,
        val forcedFormat: String?
    )
}