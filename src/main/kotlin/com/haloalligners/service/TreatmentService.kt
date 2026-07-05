package com.haloalligners.service

import com.haloalligners.model.ClinicContactsAndLabPartnersEntity
import com.haloalligners.model.TreatmentStageEntity
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import com.haloalligners.repository.TreatmentStageRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class TreatmentService(
    private val treatmentStageRepository: TreatmentStageRepository,
    private val cloudinaryService: CloudinaryService,
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository
) {

    fun uploadXrayImage(username: String, subStage: String, file: MultipartFile) {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val imageUrl = cloudinaryService.uploadFile(file)

        val treatmentStage = TreatmentStageEntity(
            user = user,
            stage = "XRAY",
            subStage = subStage,
            imageUrl = imageUrl
        )

        treatmentStageRepository.save(treatmentStage)
    }

    fun uploadArchImage(username: String, archType: String, subStage: String, file: MultipartFile) {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val imageUrl = cloudinaryService.uploadFile(file)

        val treatmentStage = TreatmentStageEntity(
            user = user,
            stage = "$archType Arch", // e.g., "Upper Arch"
            subStage = subStage,
            imageUrl = imageUrl
        )

        treatmentStageRepository.save(treatmentStage)
    }
}