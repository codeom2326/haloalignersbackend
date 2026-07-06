package com.haloalligners.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

@Service
class CloudinaryService(private val cloudinary: Cloudinary) {

    fun uploadFile(file: MultipartFile): String {
        try {
            val uploadRequest = buildUploadRequest(file.originalFilename)

            val params = ObjectUtils.asMap(
                "public_id", uploadRequest.publicId,
                "resource_type", uploadRequest.resourceType,
                "type", "upload",
                "access_mode", "public",
                "overwrite", true
            )

            val uploadResult = cloudinary.uploader().upload(file.bytes, params)
            
            return uploadResult["secure_url"] as String
        } catch (e: IOException) {
            throw RuntimeException("Could not store file ${file.originalFilename}. Please try again!", e)
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

    internal data class UploadRequest(
        val resourceType: String,
        val publicId: String
    )
}