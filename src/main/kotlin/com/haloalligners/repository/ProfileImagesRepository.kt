package com.haloalligners.repository

import com.haloalligners.model.ProfileImagesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProfileImagesRepository : JpaRepository<ProfileImagesEntity, Long> {
    fun findByCaseId(caseId: Long): Optional<ProfileImagesEntity>
}