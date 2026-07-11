package com.haloalligners.repository

import com.haloalligners.model.CaseImageMetadataEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CaseImageMetadataRepository : JpaRepository<CaseImageMetadataEntity, Long> {
    fun findByCaseId(caseId: Long): Optional<CaseImageMetadataEntity>
}