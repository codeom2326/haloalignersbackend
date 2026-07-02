package com.haloalligners.repository

import com.haloalligners.model.ClinicContactsAndLabPartnersEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ClinicContactsAndLabPartnersRepository : JpaRepository<ClinicContactsAndLabPartnersEntity, Long> {
    fun findByUsername(username: String): Optional<ClinicContactsAndLabPartnersEntity>
    fun findByEmail(email: String): Optional<ClinicContactsAndLabPartnersEntity>
}