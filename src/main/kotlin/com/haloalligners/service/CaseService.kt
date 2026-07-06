package com.haloalligners.service

import com.haloalligners.controller.CreateCaseRequest
import com.haloalligners.model.CaseEntity
import com.haloalligners.repository.CaseRepository
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CaseService(
    private val caseRepository: CaseRepository,
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository
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
            existingDisease = request.existingDisease
        )
        return caseRepository.save(newCase)
    }

    fun getAllCases(): List<CaseEntity> {
        return caseRepository.findAll()
    }

    fun updateCaseStatus(caseId: Long, newStatus: String): CaseEntity {
        val case = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        
        // You could add validation here to ensure the status transitions are valid
        // e.g., if (newStatus !in listOf("Running", "Finished")) throw ...

        case.status = newStatus
        return caseRepository.save(case)
    }
}