package com.haloalligners.repository

import com.haloalligners.model.XRayImagesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface XRayImagesRepository : JpaRepository<XRayImagesEntity, Long> {
    fun findByCaseId(caseId: Long): Optional<XRayImagesEntity>
}