package com.haloalligners.service

import com.haloalligners.model.CaseEntity
import com.haloalligners.repository.CaseRepository
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CaseService(
    private val caseRepository: CaseRepository,
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository
) {
    fun createCase(username: String, caseName: String): CaseEntity {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val newCase = CaseEntity(
            user = user,
            caseName = caseName
        )
        return caseRepository.save(newCase)
    }
}