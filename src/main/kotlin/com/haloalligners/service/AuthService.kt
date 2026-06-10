package com.haloalligners.service

import com.haloalligners.controller.AuthRequest
import com.haloalligners.controller.AuthResponse
import com.haloalligners.controller.LoginRequest
import com.haloalligners.model.UserEntity
import com.haloalligners.repository.UserRepository
import com.haloalligners.security.JwtService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) {

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

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.username,
                request.password
            )
        )
        val userDetails: UserDetails = userDetailsService.loadUserByUsername(request.username)
        val token = jwtService.generateToken(userDetails)
        return AuthResponse(token)
    }
}