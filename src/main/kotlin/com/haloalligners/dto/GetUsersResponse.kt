package com.haloalligners.dto

import java.time.LocalDate

data class GetUsersResponse(
    val id: Long,
    val username: String,
    val role: String,
    val email: String,
    val landLine: String?,
    val mobile: String,
    val preferredPartnerCrown: String?,
    val preferredPartnerImplants: String?,
    val registrationStatus: String,
    val registrationDate: LocalDate
)