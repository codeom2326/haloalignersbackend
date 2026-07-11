package com.haloalligners.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.haloalligners.constant.AppConstants
import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.CaseResponse
import com.haloalligners.model.CaseEntity
import com.haloalligners.model.RejectedCaseEntity
import com.haloalligners.service.CaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

data class CreateCaseRequest(
    val caseName: String,
    val patientName: String,
    val patientAge: Int,
    val patientGender: String,
    val existingDisease: String?
)

data class UpdateCaseStatusRequest(
    val status: String
)

@RestController
@RequestMapping("/api/cases")
class CaseController(
    private val caseService: CaseService,
    private val objectMapper: ObjectMapper
) {
    @PostMapping
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun createCase(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateCaseRequest
    ): ResponseEntity<ApiResponse<CaseResponse>> {
        val newCase = caseService.createCase(userDetails.username, request)
        val caseResponse = toCaseResponse(newCase)
        val response = ApiResponse(
            status = HttpStatus.CREATED.value(),
            message = "Case '${newCase.caseName}' created successfully.",
            data = caseResponse
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR') or hasAuthority('SUPER_ADMIN')")
    fun deleteCase(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        caseService.deleteCase(id)
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Case deleted successfully.", null))
    }

    @PostMapping("/{id}/xrays")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun uploadXrayImages(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("xrayImage1", required = false) xrayImage1: MultipartFile?,
        @RequestPart("xrayImage2", required = false) xrayImage2: MultipartFile?,
        @RequestPart("xrayImage3", required = false) xrayImage3: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        caseService.uploadXrayImages(id, userDetails.username, listOf(xrayImage1, xrayImage2, xrayImage3))
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "X-ray images uploaded successfully.", null))
    }

    @PutMapping("/{id}/xrays")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun updateXrayImages(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("xrayImage1", required = false) xrayImage1: MultipartFile?,
        @RequestPart("xrayImage2", required = false) xrayImage2: MultipartFile?,
        @RequestPart("xrayImage3", required = false) xrayImage3: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        caseService.updateXrayImages(id, userDetails.username, listOf(xrayImage1, xrayImage2, xrayImage3))
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "X-ray images updated successfully.", null))
    }

    @GetMapping("/{id}/xrays")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR') or hasAuthority('SUPER_ADMIN')")
    fun getXRayImages(@PathVariable id: Long): ResponseEntity<Any> {
        try {
            val images = caseService.getXRayImages(id)
            return ResponseEntity.ok(images)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @PostMapping("/{id}/profile-images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun uploadProfileImages(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("profileImage1", required = false) profileImage1: MultipartFile?,
        @RequestPart("profileImage2", required = false) profileImage2: MultipartFile?,
        @RequestPart("profileImage3", required = false) profileImage3: MultipartFile?,
        @RequestPart("profileImage4", required = false) profileImage4: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        caseService.uploadProfileImages(id, userDetails.username, listOf(profileImage1, profileImage2, profileImage3, profileImage4))
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Profile images uploaded successfully.", null))
    }

    @PutMapping("/{id}/profile-images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun updateProfileImages(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("profileImage1", required = false) profileImage1: MultipartFile?,
        @RequestPart("profileImage2", required = false) profileImage2: MultipartFile?,
        @RequestPart("profileImage3", required = false) profileImage3: MultipartFile?,
        @RequestPart("profileImage4", required = false) profileImage4: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        caseService.updateProfileImages(id, userDetails.username, listOf(profileImage1, profileImage2, profileImage3, profileImage4))
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Profile images updated successfully.", null))
    }

    @GetMapping("/{id}/profile-images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR') or hasAuthority('SUPER_ADMIN')")
    fun getProfileImages(@PathVariable id: Long): ResponseEntity<Any> {
        try {
            val images = caseService.getProfileImages(id)
            return ResponseEntity.ok(images)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @PostMapping("/{id}/arch-images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun uploadArchImages(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("archImage1", required = false) archImage1: MultipartFile?,
        @RequestPart("archImage2", required = false) archImage2: MultipartFile?,
        @RequestPart("archImage3", required = false) archImage3: MultipartFile?,
        @RequestPart("archImage4", required = false) archImage4: MultipartFile?,
        @RequestPart("archImage5", required = false) archImage5: MultipartFile?,
        @RequestPart("archImage6", required = false) archImage6: MultipartFile?,
        @RequestPart("archImage7", required = false) archImage7: MultipartFile?,
        @RequestPart("archImage8", required = false) archImage8: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        caseService.uploadArchImages(id, userDetails.username, listOf(archImage1, archImage2, archImage3, archImage4, archImage5, archImage6, archImage7, archImage8))
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Arch images uploaded successfully.", null))
    }

    @PutMapping("/{id}/arch-images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun updateArchImages(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("archImage1", required = false) archImage1: MultipartFile?,
        @RequestPart("archImage2", required = false) archImage2: MultipartFile?,
        @RequestPart("archImage3", required = false) archImage3: MultipartFile?,
        @RequestPart("archImage4", required = false) archImage4: MultipartFile?,
        @RequestPart("archImage5", required = false) archImage5: MultipartFile?,
        @RequestPart("archImage6", required = false) archImage6: MultipartFile?,
        @RequestPart("archImage7", required = false) archImage7: MultipartFile?,
        @RequestPart("archImage8", required = false) archImage8: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        caseService.updateArchImages(id, userDetails.username, listOf(archImage1, archImage2, archImage3, archImage4, archImage5, archImage6, archImage7, archImage8))
        return ResponseEntity.ok(ApiResponse(HttpStatus.OK.value(), "Arch images updated successfully.", null))
    }

    @GetMapping("/{id}/arch-images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR') or hasAuthority('SUPER_ADMIN')")
    fun getArchImages(@PathVariable id: Long): ResponseEntity<Any> {
        try {
            val images = caseService.getArchImages(id)
            return ResponseEntity.ok(images)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getAllCases(): ResponseEntity<List<CaseResponse>> {
        try {
            val cases = caseService.getAllCases().map { toCaseResponse(it) }
            return ResponseEntity.ok(cases)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @GetMapping("/my-cases")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR')")
    fun getAllCasesByUser(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<CaseResponse>> {
        try {
            val cases = caseService.getAllCasesForUser(userDetails.username).map { toCaseResponse(it) }
            return ResponseEntity.ok(cases)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @GetMapping("/{id}/images")
    @PreAuthorize("hasAuthority('USER') or hasAuthority('DOCTOR') or hasAuthority('SUPER_ADMIN')")
    fun getCaseImages(@PathVariable id: Long): ResponseEntity<Any> {
        try {
            val images = caseService.getCaseImages(id)
            return ResponseEntity.ok(images)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @GetMapping("/byStatus")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getCasesByStatus(@RequestParam status: String): ResponseEntity<List<CaseResponse>> {
        try {
            val cases = caseService.getCasesByStatus(status).map { toCaseResponse(it) }
            return ResponseEntity.ok(cases)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @GetMapping("/byId")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getCasesById(@RequestParam id: Long): ResponseEntity<CaseResponse> {

        try {
            val cases = toCaseResponse(caseService.getCaseById(id))
            return ResponseEntity.ok(cases)
        } catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun updateCaseStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateCaseStatusRequest
    ): ResponseEntity<ApiResponse<CaseResponse>> {
        try {
            val updatedCase = caseService.updateCaseStatus(id, request.status)
            val caseResponse = toCaseResponse(updatedCase)
            val response = ApiResponse(
                status = HttpStatus.OK.value(),
                message = "Case status updated successfully to '${request.status}'.",
                data = caseResponse
            )
            return ResponseEntity.ok(response)
        }  catch (e: Exception) {
            throw RuntimeException(AppConstants.RUNTIME_EXCEPTION_MESSAGE, e)
        }
    }

    private fun toCaseResponse(case: Any): CaseResponse {
        return if(case is RejectedCaseEntity) {
            val caseEntity = case
            CaseResponse(
                id = caseEntity.id,
                userId = caseEntity.id,
                caseName = caseEntity.caseName,
                patientName = caseEntity.patientName,
                patientAge = caseEntity.patientAge,
                patientGender = caseEntity.patientGender,
                existingDisease = caseEntity.existingDisease,
                status = caseEntity.status,
                createdAt = caseEntity.rejectedAt
            )
        } else {
            val caseEntity = case as CaseEntity
            CaseResponse(
                id = caseEntity.id,
                userId = caseEntity.user.id,
                caseName = caseEntity.caseName,
                patientName = caseEntity.patientName,
                patientAge = caseEntity.patientAge,
                patientGender = caseEntity.patientGender,
                existingDisease = caseEntity.existingDisease,
                status = caseEntity.status,
                createdAt = caseEntity.createdAt
            )
        }
    }
}