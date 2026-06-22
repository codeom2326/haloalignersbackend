package com.haloalligners.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.haloalligners.dto.ApiResponse
import com.haloalligners.service.AuthService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

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
class AuthController(
    private val authService: AuthService,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/register", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun register(
        @RequestPart("user") userJson: String,
        @RequestPart("photo") photo: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        val request = objectMapper.readValue(userJson, AuthRequest::class.java)
        return authService.registerNewUser(request, photo)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.login(request)
        val apiResponse = ApiResponse(200, "User logged in successfully", loginResponse)
        return ResponseEntity.ok(apiResponse)
    }
}