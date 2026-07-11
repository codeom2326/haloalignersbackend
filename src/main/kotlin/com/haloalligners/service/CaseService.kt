package com.haloalligners.service

import com.haloalligners.constant.AppConstants
import com.haloalligners.controller.CreateCaseRequest
import com.haloalligners.model.CaseEntity
import com.haloalligners.model.CaseImageMetadataEntity
import com.haloalligners.model.RejectedCaseEntity
import com.haloalligners.repository.CaseImageMetadataRepository
import com.haloalligners.repository.CaseRepository
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import com.haloalligners.repository.RejectedCaseRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class CaseService(
    private val caseRepository: CaseRepository,
    private val rejectedCaseRepository: RejectedCaseRepository,
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository,
    private val cloudinaryService: CloudinaryService,
    private val caseImageMetadataRepository: CaseImageMetadataRepository
) {
    fun createCase(username: String, request: CreateCaseRequest, listOfImages: List<MultipartFile?>): CaseEntity {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val newCase = CaseEntity(
            user = user,
            caseName = request.caseName,
            patientName = request.patientName,
            patientAge = request.patientAge,
            patientGender = request.patientGender,
            existingDisease = request.existingDisease,
            status = "DRAFT"
        )
        val savedCase = caseRepository.save(newCase)

        val imageUrls = listOfImages.map { it?.let { cloudinaryService.uploadFile(it) } }

        val caseImagesMetadata = CaseImageMetadataEntity(
            caseId = savedCase.id!!,
            xrayImage1Url = imageUrls[0],
            xrayImage2Url = imageUrls[1],
            xrayImage3Url = imageUrls[2],
            archImage1Url = imageUrls[3],
            archImage2Url = imageUrls[4],
            archImage3Url = imageUrls[5],
            archImage4Url = imageUrls[6],
            archImage5Url = imageUrls[7],
            archImage6Url = imageUrls[8],
            archImage7Url = imageUrls[9],
            archImage8Url = imageUrls[10],
            profileImage1Url = imageUrls[11],
            profileImage2Url = imageUrls[12],
            profileImage3Url = imageUrls[13],
            profileImage4Url = imageUrls[14]
        )
        caseImageMetadataRepository.save(caseImagesMetadata)
        
        if (imageUrls.all { it != null }) {
            savedCase.status = "Before Start"
            caseRepository.save(savedCase)
        }
        
        return savedCase
    }

    fun updateCase(caseId: Long, username: String, listOfImages: List<MultipartFile?>): CaseEntity {
        val caseToUpdate = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }

        val caseImagesMetadata = caseImageMetadataRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("Case image metadata not found for case ID $caseId") }

        val existingImageUrls = mutableListOf(
            caseImagesMetadata.xrayImage1Url,
            caseImagesMetadata.xrayImage2Url,
            caseImagesMetadata.xrayImage3Url,
            caseImagesMetadata.archImage1Url,
            caseImagesMetadata.archImage2Url,
            caseImagesMetadata.archImage3Url,
            caseImagesMetadata.archImage4Url,
            caseImagesMetadata.archImage5Url,
            caseImagesMetadata.archImage6Url,
            caseImagesMetadata.archImage7Url,
            caseImagesMetadata.archImage8Url,
            caseImagesMetadata.profileImage1Url,
            caseImagesMetadata.profileImage2Url,
            caseImagesMetadata.profileImage3Url,
            caseImagesMetadata.profileImage4Url
        )

        val updatedImageUrls = listOfImages.mapIndexed { index, file ->
            file?.let { cloudinaryService.uploadFile(it) } ?: existingImageUrls[index]
        }

        caseImagesMetadata.xrayImage1Url = updatedImageUrls[0]
        caseImagesMetadata.xrayImage2Url = updatedImageUrls[1]
        caseImagesMetadata.xrayImage3Url = updatedImageUrls[2]
        caseImagesMetadata.archImage1Url = updatedImageUrls[3]
        caseImagesMetadata.archImage2Url = updatedImageUrls[4]
        caseImagesMetadata.archImage3Url = updatedImageUrls[5]
        caseImagesMetadata.archImage4Url = updatedImageUrls[6]
        caseImagesMetadata.archImage5Url = updatedImageUrls[7]
        caseImagesMetadata.archImage6Url = updatedImageUrls[8]
        caseImagesMetadata.archImage7Url = updatedImageUrls[9]
        caseImagesMetadata.archImage8Url = updatedImageUrls[10]
        caseImagesMetadata.profileImage1Url = updatedImageUrls[11]
        caseImagesMetadata.profileImage2Url = updatedImageUrls[12]
        caseImagesMetadata.profileImage3Url = updatedImageUrls[13]
        caseImagesMetadata.profileImage4Url = updatedImageUrls[14]
        
        caseImageMetadataRepository.save(caseImagesMetadata)

        if (updatedImageUrls.all { it != null }) {
            caseToUpdate.status = "Before Start"
            caseRepository.save(caseToUpdate)
        }

        return caseToUpdate
    }

    fun getAllCases(): List<Any> {
        var allCases: MutableList<Any> = mutableListOf<Any>()
        val rejectedCases = rejectedCaseRepository.findAll()
        val otherCases = caseRepository.findAll()
        allCases.addAll(rejectedCases)
        allCases.addAll(otherCases)
        return allCases

    }

    fun getAllCasesForUser(username: String): List<CaseEntity> {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
        return caseRepository.findByUserId(user.id!!)
    }

    fun getCaseImages(caseId: Long): CaseImageMetadataEntity {
        return caseImageMetadataRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("Case image metadata not found for case ID $caseId") }
    }

    fun getCasesByStatus(status: String): List<Any> {
        return if(AppConstants.CASE_REJECTED.equals(status, ignoreCase = true)) {
            rejectedCaseRepository.findAll()
        } else {
            caseRepository.findByStatus(status)
        }

    }

    fun getCaseById(id: Long): CaseEntity {
        return caseRepository.findById(id).get()
    }

    fun updateCaseStatus(caseId: Long, newStatus: String): Any {
        val case = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        if(AppConstants.CASE_REJECTED.equals(newStatus, ignoreCase = true)){
            val rejectedCase = RejectedCaseEntity(
                username = case.user.username,
                caseName = case.caseName,
                patientName = case.patientName,
                patientAge = case.patientAge,
                patientGender = case.patientGender,
                existingDisease = case.existingDisease
            )
            caseRepository.deleteById(caseId)
            return rejectedCaseRepository.save(rejectedCase)
        }

        case.status = newStatus
        return caseRepository.save(case)
    }
}