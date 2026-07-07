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

    fun uploadFile(file: MultipartFile): String {
        if (file.size > MAX_UPLOAD_BYTES) {
            throw IllegalArgumentException("File ${file.originalFilename} exceeds the 25MB upload limit.")
        }

        try {
            val uploadRequest = buildUploadRequest(file.originalFilename)

            val params = ObjectUtils.asMap(
                "public_id", uploadRequest.publicId,
                "resource_type", uploadRequest.resourceType,
                "type", "upload",
                "access_mode", "public",
                "overwrite", true,
                "chunk_size", CHUNK_SIZE_BYTES
            )

            val uploadResult: Map<*, *>

            val shouldCompressImage = uploadRequest.resourceType == "image" && file.size > MAX_COMPRESSIBLE_IMAGE_BYTES
            val shouldCompressPdf = uploadRequest.resourceType == "raw" && file.originalFilename?.endsWith(".pdf", true) == true && file.size > PDF_COMPRESSION_THRESHOLD_BYTES

            if (shouldCompressImage) {
                val compressedBytes = maybeCompressImage(file, uploadRequest.resourceType)
                uploadResult = cloudinary.uploader().uploadLarge(compressedBytes, params)
            } else if (shouldCompressPdf) {
                val compressedBytes = compressPdf(file)
                uploadResult = cloudinary.uploader().uploadLarge(compressedBytes, params)
            } else {
                uploadResult = cloudinary.uploader().uploadLarge(file.inputStream, params)
            }
            
            return uploadResult["secure_url"] as? String ?: throw IOException("Cloudinary upload failed: secure_url not found in response.")
        } catch (e: Exception) {
            throw RuntimeException("Could not store file ${file.originalFilename}. Please try again!", e)
        }
    }

    private fun compressPdf(file: MultipartFile): ByteArray {
        try {
            Loader.loadPDF(file.bytes).use { document ->
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
            "jpg", "jpeg", "png", "gif" -> "image"
            else -> "auto"
        }
    }

    internal fun buildUploadRequest(originalFilename: String?): UploadRequest {
        val safeFilename = originalFilename?.trim().orEmpty().ifBlank { "file" }
        val extension = safeFilename.substringAfterLast('.', "").lowercase()
        val resourceType = when (extension) {
            "pdf" -> "raw"
            "jpg", "jpeg", "png", "gif" -> "image"
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

    private fun maybeCompressImage(file: MultipartFile, resourceType: String): ByteArray {
        if (resourceType != "image" || file.size <= MAX_COMPRESSIBLE_IMAGE_BYTES) {
            return file.bytes
        }

        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(ByteArrayInputStream(file.bytes))
            .size(1920, 1080)
            .outputQuality(0.8)
            .toOutputStream(outputStream)
        return outputStream.toByteArray()
    }

    internal data class UploadRequest(
        val resourceType: String,
        val publicId: String
    )
}