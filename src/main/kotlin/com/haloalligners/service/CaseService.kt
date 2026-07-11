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
    fun createCase(username: String, request: CreateCaseRequest, listOfImages: List<MultipartFile>): CaseEntity {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val newCase = CaseEntity(
            user = user,
            caseName = request.caseName,
            patientName = request.patientName,
            patientAge = request.patientAge,
            patientGender = request.patientGender,
            existingDisease = request.existingDisease
        )
        val xrayImage1Url = cloudinaryService.uploadFile(listOfImages[0])
        val xrayImage2Url = cloudinaryService.uploadFile(listOfImages[1])
        val xrayImage3Url = cloudinaryService.uploadFile(listOfImages[2])
        val archImage1Url = cloudinaryService.uploadFile(listOfImages[3])
        val archImage2Url = cloudinaryService.uploadFile(listOfImages[4])
        val archImage3Url = cloudinaryService.uploadFile(listOfImages[5])
        val archImage4Url = cloudinaryService.uploadFile(listOfImages[6])
        val archImage5Url = cloudinaryService.uploadFile(listOfImages[7])
        val archImage6Url = cloudinaryService.uploadFile(listOfImages[8])
        val archImage7Url = cloudinaryService.uploadFile(listOfImages[9])
        val archImage8Url = cloudinaryService.uploadFile(listOfImages[10])
        val profileImage1Url = cloudinaryService.uploadFile(listOfImages[11])
        val profileImage2Url = cloudinaryService.uploadFile(listOfImages[12])
        val profileImage3Url = cloudinaryService.uploadFile(listOfImages[13])
        val profileImage4Url = cloudinaryService.uploadFile(listOfImages[14])
        val savedCase = caseRepository.save(newCase)
        val caseImagesMetadata = CaseImageMetadataEntity(
            caseId = savedCase.id!!,
            xrayImage1Url = xrayImage1Url,
            xrayImage2Url = xrayImage2Url,
            xrayImage3Url = xrayImage3Url,
            archImage1Url = archImage1Url,
            archImage2Url = archImage2Url,
            archImage3Url = archImage3Url,
            archImage4Url = archImage4Url,
            archImage5Url = archImage5Url,
            archImage6Url = archImage6Url,
            archImage7Url = archImage7Url,
            archImage8Url = archImage8Url,
            profileImage1Url = profileImage1Url,
            profileImage2Url = profileImage2Url,
            profileImage3Url = profileImage3Url,
            profileImage4Url = profileImage4Url
        )
        caseImageMetadataRepository.save(caseImagesMetadata)
        return savedCase
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