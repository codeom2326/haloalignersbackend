package com.haloalligners.controller

import com.haloalligners.dto.ApiResponse
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
    ): ResponseEntity<ApiResponse<CaseEntity>> {
        val newCase = caseService.createCase(userDetails.username, request)
        val response = ApiResponse(
            status = HttpStatus.CREATED.value(),
            message = "Case '${newCase.caseName}' created successfully.",
            data = newCase
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun getAllCases(): ResponseEntity<List<CaseEntity>> {
        return ResponseEntity.ok(caseService.getAllCases())
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    fun updateCaseStatus(
        @PathVariable id: Long,
        @RequestBody request: UpdateCaseStatusRequest
    ): ResponseEntity<ApiResponse<CaseEntity>> {
        val updatedCase = caseService.updateCaseStatus(id, request.status)
        val response = ApiResponse(
            status = HttpStatus.OK.value(),
            message = "Case status updated successfully to '${updatedCase.status}'.",
            data = updatedCase
        )
        return ResponseEntity.ok(response)
    }
}