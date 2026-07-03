package com.haloalligners.service

import com.haloalligners.model.PatientCaseEntity
import com.haloalligners.repository.PatientCaseRepository
import com.haloalligners.request.CaseRequest
import com.haloalligners.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CaseService(private val cloudinaryService: CloudinaryService,
    private val patientCaseRepository: PatientCaseRepository) {
    fun createCase(
        request: CaseRequest,
        xrayImage1: MultipartFile,
        xrayImage2: MultipartFile,
        xrayImage3: MultipartFile,
        archImage1: MultipartFile,
        archImage2: MultipartFile,
        archImage3: MultipartFile,
        archImage4: MultipartFile,
        archImage5: MultipartFile,
        archImage6: MultipartFile,
        archImage7: MultipartFile,
        archImage8: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        try {
            val xrayImage1Url = cloudinaryService.uploadFile(xrayImage1)
            val xrayImage2Url = cloudinaryService.uploadFile(xrayImage2)
            val xrayImage3Url = cloudinaryService.uploadFile(xrayImage3)
            val archImage1Url = cloudinaryService.uploadFile(archImage1)
            val archImage2Url = cloudinaryService.uploadFile(archImage2)
            val archImage3Url = cloudinaryService.uploadFile(archImage3)
            val archImage4Url = cloudinaryService.uploadFile(archImage4)
            val archImage5Url = cloudinaryService.uploadFile(archImage5)
            val archImage6Url = cloudinaryService.uploadFile(archImage6)
            val archImage7Url = cloudinaryService.uploadFile(archImage7)
            val archImage8Url = cloudinaryService.uploadFile(archImage8)

            val patientCase = PatientCaseEntity(
                doctorUsername = request.doctorUsername,
                doctorName = request.doctorName,
                patientName = request.patientName,
                patientAge = request.patientAge,
                patientSex = request.patientSex,
                preExistingDesease = request.preExistingDesease,
                xray1ImageUrl = xrayImage1Url,
                xray2ImageUrl = xrayImage2Url,
                xray3ImageUrl = xrayImage3Url,
                archImage1Url = archImage1Url,
                archImage2Url = archImage2Url,
                archImage3Url = archImage3Url,
                archImage4Url = archImage4Url,
                archImage5Url = archImage5Url,
                archImage6Url = archImage6Url,
                archImage7Url = archImage7Url,
                archImage8Url = archImage8Url
            )
            patientCaseRepository.save(patientCase)
            val response = ApiResponse<Unit>(
                status = HttpStatus.CREATED.value(),
                message = "User registered successfully",
                data = null
            )
            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: Exception) {
            throw RuntimeException("Exception occurred while Case Creation.")
        }
    }
}