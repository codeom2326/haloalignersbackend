package com.haloalligners.service

import com.haloalligners.controller.AuthRequest
import com.haloalligners.controller.LoginRequest
import com.haloalligners.controller.LoginResponse
import com.haloalligners.dto.ApiResponse
import com.haloalligners.model.UserEntity
import com.haloalligners.repository.UserRepository
import com.haloalligners.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
    private val cloudinaryService: CloudinaryService
) {

    fun registerNewUser(request: AuthRequest, photo: MultipartFile): ResponseEntity<ApiResponse<Unit>>{
        if (userRepository.findByUsername(request.username) != null) {
            val response = ApiResponse<Unit>(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Username already exists",
                data = null
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
        }
        if (userRepository.findByEmail(request.email) != null) {
            val response = ApiResponse<Unit>(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Email already exists",
                data = null
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
        }

        val photoUrl = cloudinaryService.uploadFile(photo)

        val newUser = UserEntity(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = "USER",
            fullName = request.fullName,
            email = request.email,
            phone = request.phone,
            gstNumber = request.gstNumber,
            clinicName = request.clinicName,
            photoUrl = photoUrl
        )
        userRepository.save(newUser)
        
        val response = ApiResponse<Unit>(
            status = HttpStatus.CREATED.value(),
            message = "User registered successfully",
            data = null
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    fun login(request: LoginRequest): LoginResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.username,
                request.password
            )
        )
        val userDetails: UserDetails = userDetailsService.loadUserByUsername(request.username)
        val token = jwtService.generateToken(userDetails)
        
        val user = userRepository.findByUsername(request.username)
            ?: throw RuntimeException("User not found")
        
        val userInfo = com.haloalligners.controller.UserInfo(
            id = user.id!!,
            username = user.username,
            fullName = user.fullName,
            email = user.email,
            phone = user.phone,
            gstNumber = user.gstNumber,
            clinicName = user.clinicName,
            photoUrl = user.photoUrl,
            role = user.role
        )
        
        return LoginResponse(token, userInfo)
    }
}