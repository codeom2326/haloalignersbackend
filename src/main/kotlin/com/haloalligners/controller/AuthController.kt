package com.haloalligners.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.haloalligners.response.ApiResponse
import com.haloalligners.response.GetSingleUserResponse
import com.haloalligners.response.GetUsersResponse
import com.haloalligners.request.AuthRequest
import com.haloalligners.request.LoginRequest
import com.haloalligners.response.LoginResponse
import com.haloalligners.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val objectMapper: ObjectMapper
) {

    //changes registration

    @PostMapping("/register", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun register(
        @RequestPart("user") userJson: String,
        @RequestPart("addressProof") addressProof: MultipartFile,
        @RequestPart("gstCertificate") gstCertificate: MultipartFile,
        @RequestPart("pan") pan: MultipartFile,
        @RequestPart("registrationCertificate") registrationCertificate: MultipartFile,
        @RequestPart("letterheadOrVisitingCard") letterheadOrVisitingCard: MultipartFile,
        @RequestPart("signatureOrStamp") signatureOrStamp: MultipartFile,
        @RequestPart("photo") photo: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        val request = objectMapper.readValue(userJson, AuthRequest::class.java)
        return authService.registerNewUser(request,
            addressProof, gstCertificate, pan,
            registrationCertificate, letterheadOrVisitingCard,
            signatureOrStamp, photo)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.login(request)
        val apiResponse = ApiResponse<LoginResponse>(200, "User logged in successfully", loginResponse)
        return ResponseEntity.ok(apiResponse)
    }

    @GetMapping("/users")
    fun getUsersByStatus(@RequestParam requestStatus: String): ResponseEntity<List<GetUsersResponse>> {
        return ResponseEntity(authService.getUsers(requestStatus), HttpStatus.OK)
    }

    @GetMapping("/user")
    fun getUserById(@RequestParam id: Long): ResponseEntity<GetSingleUserResponse> {
        return ResponseEntity(authService.getUser(id), HttpStatus.OK)
    }

    @PutMapping("/users")
    fun updateUser(@RequestParam id: Long, @RequestParam status: String
    ): ResponseEntity<ApiResponse<Unit>> {
        return authService.updateUserStatus(id, status)
    }
}