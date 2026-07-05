package com.haloalligners.repository

import com.haloalligners.model.PractitionerDetailsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PractitionerDetailsRepository : JpaRepository<PractitionerDetailsEntity, Long> {
    fun findByPan(pan: String): Optional<PractitionerDetailsEntity>
}