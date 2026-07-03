package com.haloalligners.response

import com.haloalligners.dto.UserInfo

data class LoginResponse(
    val token: String,
    val user: UserInfo
)
