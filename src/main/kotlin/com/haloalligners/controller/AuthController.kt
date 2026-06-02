package com.haloalligners.controller

import com.haloalligners.model.UserEntity
import com.haloalligners.repository.UserRepository
import com.haloalligners.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

data class AuthRequest(val username: String, val password: String, val userRole: String)

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: AuthRequest): ResponseEntity<String> {
        return authService.registerNewUser(request)
    }
}