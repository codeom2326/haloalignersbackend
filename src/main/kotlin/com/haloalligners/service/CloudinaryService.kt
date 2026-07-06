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
            val resourceType = when {
                file.contentType?.startsWith("image") == true -> "image"
                file.contentType == "application/pdf" -> "raw"
                else -> "auto"
            }

            // Manually create a unique public_id to preserve the filename
            val originalFilename = file.originalFilename ?: "file"
            val baseName = originalFilename.substringBeforeLast('.')
            val extension = originalFilename.substringAfterLast('.', "")
            val publicId = "${baseName}_${UUID.randomUUID().toString().substring(0, 6)}"

            val params = ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", resourceType
            )

            val uploadResult = cloudinary.uploader().upload(file.bytes, params)
            
            // The URL will now include the extension, e.g., .../publicId.pdf
            return uploadResult["secure_url"] as String
        } catch (e: IOException) {
            throw RuntimeException("Could not store file ${file.originalFilename}. Please try again!", e)
        }
    }
}