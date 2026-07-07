package com.haloalligners.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

@Service
class CloudinaryService(private val cloudinary: Cloudinary) {

    private val MAX_SIZE_BYTES = 2 * 1024 * 1024 // 2MB

    fun uploadFile(file: MultipartFile): String {
        try {
            var fileBytes = file.bytes
            val originalFilename = file.originalFilename ?: "file"
            val extension = originalFilename.substringAfterLast('.', "").lowercase()
            val resourceType = when (extension) {
                "pdf" -> "raw"
                "jpg", "jpeg", "png", "gif" -> "image"
                else -> "auto"
            }

            // Compress image if it's an image and exceeds the size limit
            if (resourceType == "image" && file.size > MAX_SIZE_BYTES) {
                val outputStream = ByteArrayOutputStream()
                Thumbnails.of(ByteArrayInputStream(fileBytes))
                    .size(1920, 1080) // Resize to a reasonable resolution
                    .outputQuality(0.8) // Adjust quality to reduce size
                    .toOutputStream(outputStream)
                fileBytes = outputStream.toByteArray()
            }

            val baseName = originalFilename.substringBeforeLast('.', "")
            val publicId = if (extension.isNotEmpty()) {
                "${baseName}_${UUID.randomUUID().toString().substring(0, 6)}.$extension"
            } else {
                "${baseName}_${UUID.randomUUID().toString().substring(0, 6)}"
            }

            val params = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", resourceType,
                "overwrite", true
            )

            val uploadResult = cloudinary.uploader().upload(fileBytes, params)
            
            return uploadResult["secure_url"] as String
        } catch (e: IOException) {
            throw RuntimeException("Could not store file ${file.originalFilename}. Please try again!", e)
        }
    }
}