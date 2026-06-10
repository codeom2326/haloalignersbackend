package com.haloalligners.service

import com.haloalligners.controller.AuthRequest
import com.haloalligners.model.UserEntity
import com.haloalligners.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder) {

    fun registerNewUser(request: AuthRequest): ResponseEntity<String>{
        if (userRepository.findByUsername(request.username) != null) {
            return ResponseEntity.badRequest().body("Username already exists")
        }
        val newUser = UserEntity(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = request.userRole,
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            gstNumber = request.gstNumber,
            clinicName = request.clinicName
        )
        userRepository.save(newUser)
        return ResponseEntity.ok("User registered successfully")
    }
}