package com.haloalligners.service

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileStorageService {

    private val fileStorageLocation: Path

    init {
        // Defines the directory where files will be stored.
        fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize()
        try {
            Files.createDirectories(this.fileStorageLocation)
        } catch (ex: Exception) {
            throw RuntimeException("Could not create the directory where the uploaded files will be stored.", ex)
        }
    }

    fun storeFile(file: MultipartFile): String {
        // Normalize file name
        val originalFileName = StringUtils.cleanPath(file.originalFilename ?: "")
        
        // Generate a unique file name to avoid overwriting existing files
        val uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName

        try {
            // Check if the file's name contains invalid characters
            if (uniqueFileName.contains("..")) {
                throw RuntimeException("Sorry! Filename contains invalid path sequence $uniqueFileName")
            }

            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = this.fileStorageLocation.resolve(uniqueFileName)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return uniqueFileName
        } catch (ex: IOException) {
            throw RuntimeException("Could not store file $uniqueFileName. Please try again!", ex)
        }
    }

    fun loadFileAsResource(fileName: String): Resource {
        try {
            val filePath = this.fileStorageLocation.resolve(fileName).normalize()
            val resource: Resource = UrlResource(filePath.toUri())
            if (resource.exists()) {
                return resource
            } else {
                throw RuntimeException("File not found $fileName")
            }
        } catch (ex: MalformedURLException) {
            throw RuntimeException("File not found $fileName", ex)
        }
    }
}