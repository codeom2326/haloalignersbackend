package com.haloalligners.service

import com.haloalligners.constant.AppConstants
import com.haloalligners.controller.CreateCaseRequest
import com.haloalligners.model.*
import com.haloalligners.repository.*
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
    private val xRayImagesRepository: XRayImagesRepository,
    private val profileImagesRepository: ProfileImagesRepository,
    private val archImagesRepository: ArchImagesRepository
) {
    fun createCase(username: String, request: CreateCaseRequest): CaseEntity {
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
        return caseRepository.save(newCase)
    }

    fun uploadXrayImages(caseId: Long, username: String, images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val xRayImages = xRayImagesRepository.findByCaseId(caseId).orElse(XRayImagesEntity(case = caseEntity))
        
        images.forEachIndexed { index, file ->
            file?.takeUnless { it.isEmpty }?.let {
                when (index) {
                    0 -> xRayImages.xrayImage1Url = cloudinaryService.uploadFile(it)
                    1 -> xRayImages.xrayImage2Url = cloudinaryService.uploadFile(it)
                    2 -> xRayImages.xrayImage3Url = cloudinaryService.uploadFile(it)
                }
            }
        }
        xRayImagesRepository.save(xRayImages)
        updateCaseStatusIfComplete(caseEntity)
    }

    fun updateXrayImages(caseId: Long, username: String, images: List<MultipartFile?>) {
        uploadXrayImages(caseId, username, images)
    }

    fun uploadProfileImages(caseId: Long, username: String, images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val profileImages = profileImagesRepository.findByCaseId(caseId).orElse(ProfileImagesEntity(case = caseEntity))

        images.forEachIndexed { index, file ->
            file?.takeUnless { it.isEmpty }?.let {
                when (index) {
                    0 -> profileImages.profileImage1Url = cloudinaryService.uploadFile(it)
                    1 -> profileImages.profileImage2Url = cloudinaryService.uploadFile(it)
                    2 -> profileImages.profileImage3Url = cloudinaryService.uploadFile(it)
                    3 -> profileImages.profileImage4Url = cloudinaryService.uploadFile(it)
                }
            }
        }
        profileImagesRepository.save(profileImages)
        updateCaseStatusIfComplete(caseEntity)
    }

    fun updateProfileImages(caseId: Long, username: String, images: List<MultipartFile?>) {
        uploadProfileImages(caseId, username, images)
    }

    fun uploadArchImages(caseId: Long, username: String, images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val archImages = archImagesRepository.findByCaseId(caseId).orElse(ArchImagesEntity(case = caseEntity))

        images.forEachIndexed { index, file ->
            file?.takeUnless { it.isEmpty }?.let {
                when (index) {
                    0 -> archImages.archImage1Url = cloudinaryService.uploadFile(it)
                    1 -> archImages.archImage2Url = cloudinaryService.uploadFile(it)
                    2 -> archImages.archImage3Url = cloudinaryService.uploadFile(it)
                    3 -> archImages.archImage4Url = cloudinaryService.uploadFile(it)
                    4 -> archImages.archImage5Url = cloudinaryService.uploadFile(it)
                    5 -> archImages.archImage6Url = cloudinaryService.uploadFile(it)
                    6 -> archImages.archImage7Url = cloudinaryService.uploadFile(it)
                    7 -> archImages.archImage8Url = cloudinaryService.uploadFile(it)
                }
            }
        }
        archImagesRepository.save(archImages)
        updateCaseStatusIfComplete(caseEntity)
    }

    fun updateArchImages(caseId: Long, username: String, images: List<MultipartFile?>) {
        uploadArchImages(caseId, username, images)
    }

    private fun updateCaseStatusIfComplete(caseEntity: CaseEntity) {
        val xrays = xRayImagesRepository.findByCaseId(caseEntity.id!!).orElse(null)
        val profiles = profileImagesRepository.findByCaseId(caseEntity.id!!).orElse(null)
        val arches = archImagesRepository.findByCaseId(caseEntity.id!!).orElse(null)

        if (xrays?.xrayImage1Url != null && xrays.xrayImage2Url != null && xrays.xrayImage3Url != null &&
            profiles?.profileImage1Url != null && profiles.profileImage2Url != null && profiles.profileImage3Url != null && profiles.profileImage4Url != null &&
            arches?.archImage1Url != null && arches.archImage2Url != null && arches.archImage3Url != null && arches.archImage4Url != null &&
            arches.archImage5Url != null && arches.archImage6Url != null && arches.archImage7Url != null && arches.archImage8Url != null) {
            caseEntity.status = "Before Start"
            caseRepository.save(caseEntity)
        }
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

    fun getCaseImages(caseId: Long): Map<String, Any?> {
        val xrays = xRayImagesRepository.findByCaseId(caseId).orElse(null)
        val profiles = profileImagesRepository.findByCaseId(caseId).orElse(null)
        val arches = archImagesRepository.findByCaseId(caseId).orElse(null)
        return mapOf(
            "xrays" to xrays,
            "profiles" to profiles,
            "arches" to arches
        )
    }

    fun getXRayImages(caseId: Long): XRayImagesEntity {
        return xRayImagesRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("X-Ray images not found for case ID $caseId") }
    }

    fun getProfileImages(caseId: Long): ProfileImagesEntity {
        return profileImagesRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("Profile images not found for case ID $caseId") }
    }

    fun getArchImages(caseId: Long): ArchImagesEntity {
        return archImagesRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("Arch images not found for case ID $caseId") }
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

    fun deleteCase(caseId: Long) {
        val case = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }

        case.xrayImages?.let {
            it.xrayImage1Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.xrayImage2Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.xrayImage3Url?.let { url -> cloudinaryService.deleteFile(url) }
        }
        case.profileImages?.let {
            it.profileImage1Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.profileImage2Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.profileImage3Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.profileImage4Url?.let { url -> cloudinaryService.deleteFile(url) }
        }
        case.archImages?.let {
            it.archImage1Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage2Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage3Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage4Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage5Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage6Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage7Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage8Url?.let { url -> cloudinaryService.deleteFile(url) }
        }

        caseRepository.delete(case)
    }
}