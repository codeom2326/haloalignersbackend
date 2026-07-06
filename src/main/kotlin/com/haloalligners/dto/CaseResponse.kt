package com.haloalligners.dto

import java.time.Instant

data class CaseResponse(
    val id: Long?,
    val userId: Long?,
    val caseName: String,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val existingDisease: String?,
    val status: String,
    val createdAt: Instant
)