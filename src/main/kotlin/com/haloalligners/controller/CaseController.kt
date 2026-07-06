package com.haloalligners.controller

import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.CaseResponse
import com.haloalligners.model.CaseEntity
import com.haloalligners.service.CaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

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
    private val caseService: CaseService
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

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getAllCases(): ResponseEntity<List<CaseResponse>> {
        val cases = caseService.getAllCases().map { toCaseResponse(it) }
        return ResponseEntity.ok(cases)
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun updateCaseStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateCaseStatusRequest
    ): ResponseEntity<ApiResponse<CaseResponse>> {
        val updatedCase = caseService.updateCaseStatus(id, request.status)
        val caseResponse = toCaseResponse(updatedCase)
        val response = ApiResponse(
            status = HttpStatus.OK.value(),
            message = "Case status updated successfully to '${updatedCase.status}'.",
            data = caseResponse
        )
        return ResponseEntity.ok(response)
    }

    private fun toCaseResponse(caseEntity: CaseEntity): CaseResponse {
        return CaseResponse(
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