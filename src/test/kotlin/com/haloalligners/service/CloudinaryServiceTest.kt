package com.haloalligners.service

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloudinaryServiceTest {

    private val service = CloudinaryService(Cloudinary(ObjectUtils.emptyMap()))

    @Test
    fun `buildUploadRequest keeps pdf extension on raw assets`() {
        val uploadRequest = service.buildUploadRequest("Certificate for Additional.pdf")

        assertEquals("raw", uploadRequest.resourceType)
        assertTrue(uploadRequest.publicId.matches(Regex("certificate-for-additional_[0-9a-f]{6}\\.pdf")))
    }

    @Test
    fun `buildUploadRequest omits image extensions from public id`() {
        val uploadRequest = service.buildUploadRequest("Doctor Photo.JPG")

        assertEquals("image", uploadRequest.resourceType)
        assertTrue(uploadRequest.publicId.matches(Regex("doctor-photo_[0-9a-f]{6}")))
        assertFalse(uploadRequest.publicId.endsWith(".jpg"))
    }

    @Test
    fun `buildUploadRequest treats heic files as images`() {
        val uploadRequest = service.buildUploadRequest("Scan.HEIC")

        assertEquals("image", uploadRequest.resourceType)
        assertTrue(uploadRequest.publicId.matches(Regex("scan_[0-9a-f]{6}")))
        assertFalse(uploadRequest.publicId.endsWith(".heic"))
    }

    @Test
    fun `normalizePublicIdSegment falls back to file for blank names`() {
        assertEquals("file", service.normalizePublicIdSegment("   "))
    }
}
