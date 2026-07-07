package com.haloalligners.config

import com.cloudinary.Cloudinary
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CloudinaryConfig {

    @Value("\${CLOUDINARY_URL}")
    private lateinit var cloudinaryUrl: String

    @Value("\${cloudinary.timeout-ms:60000}")
    private var timeoutMs: Int = 60000

    @Bean
    fun cloudinary(): Cloudinary {
        val cloudinary = Cloudinary(cloudinaryUrl)
        cloudinary.config.secure = true
        cloudinary.config.timeout = timeoutMs
        return cloudinary
    }
}