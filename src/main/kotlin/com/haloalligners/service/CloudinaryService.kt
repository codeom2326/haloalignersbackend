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
            val uploadResult = cloudinary.uploader().upload(file.bytes, ObjectUtils.emptyMap())
            return uploadResult["secure_url"] as String
        } catch (e: IOException) {
            throw RuntimeException("Could not store file ${file.originalFilename}. Please try again!", e)
        }
    }
}