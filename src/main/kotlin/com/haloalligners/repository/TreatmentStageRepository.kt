package com.haloalligners.repository

import com.haloalligners.model.TreatmentStageEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TreatmentStageRepository : JpaRepository<TreatmentStageEntity, Long>
