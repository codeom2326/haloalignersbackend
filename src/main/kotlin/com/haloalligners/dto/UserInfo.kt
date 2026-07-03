package com.haloalligners.dto

data class UserInfo(
    val id: Long,
    val userName: String,
    val password: String,
    val role: String,
    val landLine: String?,
    val mobile: String,
    val email : String,
    val preferredPartnerCrown: String?,
    val preferredPartnerImplants: String?
)
