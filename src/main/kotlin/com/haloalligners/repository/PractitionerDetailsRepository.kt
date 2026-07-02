package com.haloalligners.repository

import com.haloalligners.model.PractitionerDetailsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PractitionerDetailsRepository : JpaRepository<PractitionerDetailsEntity, Long>