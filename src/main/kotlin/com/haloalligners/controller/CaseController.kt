package com.haloalligners.controller

import com.haloalligners.dto.ApiResponse
import com.haloalligners.model.CaseEntity
import com.haloalligners.service.CaseService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class CreateCaseRequest(
    val caseName: String
)

@RestController
@RequestMapping("/api/cases")
class CaseController(
    private val caseService: CaseService
) {
    @PostMapping
    fun createCase(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody request: CreateCaseRequest
    ): ResponseEntity<ApiResponse<CaseEntity>> {
        val newCase = caseService.createCase(userDetails.username, request.caseName)
        val response = ApiResponse(
            status = HttpStatus.CREATED.value(),
            message = "Case '${newCase.caseName}' created successfully.",
            data = newCase
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}