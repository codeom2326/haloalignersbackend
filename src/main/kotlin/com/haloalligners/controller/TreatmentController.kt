package com.haloalligners.controller

import com.haloalligners.dto.ApiResponse
import com.haloalligners.service.TreatmentService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/treatment")
class TreatmentController(
    private val treatmentService: TreatmentService
) {

    @PostMapping("/upload-xray", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadXrayImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("sub_stage") subStage: String,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        treatmentService.uploadXrayImage(userDetails.username, subStage, file)
        val response = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "X-ray image uploaded successfully for sub-stage '$subStage'.",
            data = null
        )
        return ResponseEntity.ok(response)
    }

    @PostMapping("/upload-arch", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadArchImage(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestPart("arch_type") archType: String, // "Upper" or "Lower"
        @RequestPart("sub_stage") subStage: String, // "Upper_1", "Lower_3", etc.
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        treatmentService.uploadArchImage(userDetails.username, archType, subStage, file)
        val response = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "$archType arch image uploaded successfully for sub-stage '$subStage'.",
            data = null
        )
        return ResponseEntity.ok(response)
    }
}