package com.haloalligners.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.GetSingleUserResponse
import com.haloalligners.dto.GetUsersResponse
import com.haloalligners.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
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
    val addressLine4: String,
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
        val apiResponse = ApiResponse(200, "User logged in successfully", loginResponse)
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