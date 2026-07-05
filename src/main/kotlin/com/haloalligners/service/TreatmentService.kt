package com.haloalligners.service

import com.haloalligners.model.TreatmentStageEntity
import com.haloalligners.repository.CaseRepository
import com.haloalligners.repository.TreatmentStageRepository
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import jakarta.persistence.EntityNotFoundException

@Service
class TreatmentService(
    private val treatmentStageRepository: TreatmentStageRepository,
    private val cloudinaryService: CloudinaryService,
    private val caseRepository: CaseRepository
) {

    fun uploadXrayImage(currentUser: UserDetails, caseId: Long, subStage: String, file: MultipartFile) {
        val case = getCaseForUpload(currentUser, caseId)
        val imageUrl = cloudinaryService.uploadFile(file)
        val treatmentStage = TreatmentStageEntity(
            case = case,
            stage = "XRAY",
            subStage = subStage,
            imageUrl = imageUrl
        )
        treatmentStageRepository.save(treatmentStage)
    }

    fun uploadArchImage(currentUser: UserDetails, caseId: Long, archType: String, subStage: String, file: MultipartFile) {
        val case = getCaseForUpload(currentUser, caseId)
        val imageUrl = cloudinaryService.uploadFile(file)
        val treatmentStage = TreatmentStageEntity(
            case = case,
            stage = "$archType Arch",
            subStage = subStage,
            imageUrl = imageUrl
        )
        treatmentStageRepository.save(treatmentStage)
    }

    fun uploadProfileImage(currentUser: UserDetails, caseId: Long, imageType: String, file: MultipartFile) {
        val case = getCaseForUpload(currentUser, caseId)
        val imageUrl = cloudinaryService.uploadFile(file)
        val treatmentStage = TreatmentStageEntity(
            case = case,
            stage = "Profile",
            subStage = imageType,
            imageUrl = imageUrl
        )
        treatmentStageRepository.save(treatmentStage)
    }

    private fun getCaseForUpload(currentUser: UserDetails, caseId: Long): com.haloalligners.model.CaseEntity {
        val case = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }

        val isSuperAdmin = currentUser.authorities.any { it.authority == "SUPER_ADMIN" }
        
        // A user can only upload to their own case, unless they are a SUPER_ADMIN
        if (case.user.username != currentUser.username && !isSuperAdmin) {
            throw AccessDeniedException("You do not have permission to upload images to this case.")
        }
        
        return case
    }
}