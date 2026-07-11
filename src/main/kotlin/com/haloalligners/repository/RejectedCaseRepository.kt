package com.haloalligners.repository

import com.haloalligners.model.RejectedCaseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RejectedCaseRepository: JpaRepository<RejectedCaseEntity, Long> {
    fun findByRejectedAtAfter(cutoffDate: LocalDateTime): List<RejectedCaseEntity>
}