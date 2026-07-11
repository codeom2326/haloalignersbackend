package com.haloalligners.repository

import com.haloalligners.model.ArchImagesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ArchImagesRepository : JpaRepository<ArchImagesEntity, Long> {
    fun findByCaseId(caseId: Long): Optional<ArchImagesEntity>
}