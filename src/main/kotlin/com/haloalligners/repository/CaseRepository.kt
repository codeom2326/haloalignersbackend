package com.haloalligners.repository

import com.haloalligners.model.CaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CaseRepository : JpaRepository<CaseEntity, Long> {
    fun findByStatus(status: String): List<CaseEntity>
}
