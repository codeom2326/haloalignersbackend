package com.haloalligners.repository

import com.haloalligners.model.PatientCaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientCaseRepository : JpaRepository<PatientCaseEntity, Long> {
}