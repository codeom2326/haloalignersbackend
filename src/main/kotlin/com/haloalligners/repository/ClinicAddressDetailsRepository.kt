package com.haloalligners.repository

import com.haloalligners.model.ClinicAddressDetailsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClinicAddressDetailsRepository : JpaRepository<ClinicAddressDetailsEntity, Long>