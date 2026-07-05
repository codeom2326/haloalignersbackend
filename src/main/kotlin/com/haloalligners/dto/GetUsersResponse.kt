package com.haloalligners.dto

data class GetUsersResponse(
    val id: Long,
    val username: String,
    val role: String,
    val email: String,
    val landLine: String?,
    val mobile: String,
    val preferredPartnerCrown: String?,
    val preferredPartnerImplants: String?,
    val registrationStatus: String
)