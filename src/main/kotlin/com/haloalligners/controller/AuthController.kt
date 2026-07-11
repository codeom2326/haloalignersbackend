package com.haloalligners.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.GetSingleUserResponse
import com.haloalligners.dto.GetUsersResponse
import com.haloalligners.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

data class AuthRequest(
    val username: String,
    val password: String,
    val role: String,
    val landLine: String,
    val mobile: String,
    val email : String,
    val preferredPartnerCrown: String,
    val preferredPartnerImplants: String,
    val registrationStatus: String,
    val fullName: String,
    val doctorRegistrationNumber: String,
    val pan: String,
    val practitionerCategory: String,
    val businessArea: String,
    val clinicName: String,
    val addressLine1: String,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val addressLine4: String? = null,
    val addressLine5: String,
    val isDispatchAddressSameAsInvoice: Boolean,
    val addressProofType: String,
    val isClinicGstRegistered: Boolean = false,
    val gstNumber: String?,
    val doctorRegistrationCertificate: Boolean? = false,
    val letterHeadOrVisitingCard: Boolean? = false,
    val panCard: Boolean? = true
)



data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: UserInfo
)

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

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val objectMapper: ObjectMapper
) {

    @PostMapping("/register", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun register(
        @RequestPart("user") userJson: String,
        @RequestPart("photo") photo: MultipartFile,
        @RequestPart("addressProof", required = false) addressProof: MultipartFile?,
        @RequestPart("gstCertificate", required = false) gstCertificate: MultipartFile?,
        @RequestPart("pan", required = false) pan: MultipartFile?,
        @RequestPart("registrationCertificate", required = false) registrationCertificate: MultipartFile?,
        @RequestPart("letterheadOrVisitingCard", required = false) letterheadOrVisitingCard: MultipartFile?,
        @RequestPart("signatureOrStamp", required = false) signatureOrStamp: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        val request = objectMapper.readValue(userJson, AuthRequest::class.java)
        return authService.registerNewUser(
            request,
            photo,
            addressProof,
            gstCertificate,
            pan,
            registrationCertificate,
            letterheadOrVisitingCard,
            signatureOrStamp
        )
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.login(request)
        val apiResponse = ApiResponse(200, "User logged in successfully", loginResponse)
        return ResponseEntity.ok(apiResponse)
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getUsersByStatus(@RequestParam(required = false) requestStatus: String?): ResponseEntity<List<GetUsersResponse>> {
        return ResponseEntity(authService.getUsers(requestStatus), HttpStatus.OK)
    }

    @GetMapping("/users/by-case-status")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getUsersByCaseStatus(@RequestParam status: String): ResponseEntity<List<GetUsersResponse>> {
        return ResponseEntity(authService.getUsersByCaseStatus(status), HttpStatus.OK)
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getUserById(@RequestParam id: Long): ResponseEntity<GetSingleUserResponse> {
        return ResponseEntity(authService.getUser(id), HttpStatus.OK)
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun updateUser(
        @PathVariable id: Long,
        @RequestPart("user") userJson: String,
        @RequestPart("photo", required = false) photo: MultipartFile?,
        @RequestPart("addressProof", required = false) addressProof: MultipartFile?,
        @RequestPart("gstCertificate", required = false) gstCertificate: MultipartFile?,
        @RequestPart("pan", required = false) pan: MultipartFile?,
        @RequestPart("registrationCertificate", required = false) registrationCertificate: MultipartFile?,
        @RequestPart("letterheadOrVisitingCard", required = false) letterheadOrVisitingCard: MultipartFile?,
        @RequestPart("signatureOrStamp", required = false) signatureOrStamp: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        val request = objectMapper.readValue(userJson, AuthRequest::class.java)
        return authService.updateUser(
            id,
            request,
            photo,
            addressProof,
            gstCertificate,
            pan,
            registrationCertificate,
            letterheadOrVisitingCard,
            signatureOrStamp
        )
    }

    @PutMapping("/users")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun updateUserStatus(
        @RequestParam id: Long, 
        @RequestParam status: String,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<ApiResponse<Unit>> {
        return authService.updateUserStatus(id, status, reason)
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        return authService.deleteUser(id)
    }
}