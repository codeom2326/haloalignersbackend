package com.haloalligners.repository

import com.haloalligners.model.CaseImageMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CaseImageMetadataRepository : JpaRepository<CaseImageMetadataEntity, Long> {
    fun findByCaseId(caseId: Long): CaseImageMetadataEntity
}