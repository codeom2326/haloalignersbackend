package com.haloalligners.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class CloudinaryService(private val cloudinary: Cloudinary) {

    fun uploadFile(file: MultipartFile): String {
        try {
            val resourceType = when {
                file.contentType?.startsWith("image") == true -> "image"
                file.contentType == "application/pdf" -> "raw"
                else -> "auto"
            }

            val params = ObjectUtils.asMap(
                "resource_type", resourceType,
                "use_filename", true,
                "unique_filename", true
            )

            val uploadResult = cloudinary.uploader().upload(file.bytes, params)
            return uploadResult["secure_url"] as String
        } catch (e: IOException) {
            throw RuntimeException("Could not store file ${file.originalFilename}. Please try again!", e)
        }
    }
}