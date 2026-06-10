package com.haloalligners.controller

import com.haloalligners.dto.ApiResponse
import com.haloalligners.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AuthRequest(
    val username: String,
    val password: String,
    val userRole: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val gstNumber: String?,
    val clinicName: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<ApiResponse<Unit>> {
        return authService.registerNewUser(request)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.login(request)
        val apiResponse = ApiResponse(200, "User logged in successfully", loginResponse)
        return ResponseEntity.ok(apiResponse)
    }
}