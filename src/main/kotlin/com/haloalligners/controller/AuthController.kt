package com.haloalligners.controller

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

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<String> {
        return authService.registerNewUser(request)
    }
}