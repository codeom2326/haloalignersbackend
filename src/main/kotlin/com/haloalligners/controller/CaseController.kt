package com.haloalligners.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.haloalligners.request.AuthRequest
import com.haloalligners.request.CaseRequest
import com.haloalligners.response.ApiResponse
import com.haloalligners.service.CaseService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/user")
class CaseController(private val caseService: CaseService,
                     private val objectMapper: ObjectMapper) {

    @PostMapping("/case", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createCase(@RequestPart("case") userJson: String,
                   @RequestPart("xrayImage1") xrayImage1: MultipartFile,
                   @RequestPart("xrayImage2") xrayImage2: MultipartFile,
                   @RequestPart("xrayImage3") xrayImage3: MultipartFile,
                   @RequestPart("archImage1") archImage1: MultipartFile,
                   @RequestPart("archImage2") archImage2: MultipartFile,
                   @RequestPart("archImage3") archImage3: MultipartFile,
                   @RequestPart("archImage4") archImage4: MultipartFile,
                   @RequestPart("archImage5") archImage5: MultipartFile,
                   @RequestPart("archImage6") archImage6: MultipartFile,
                   @RequestPart("archImage7") archImage7: MultipartFile,
                   @RequestPart("archImage8") archImage8: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>> {
        val request = objectMapper.readValue(userJson, CaseRequest::class.java)
        return caseService.createCase(request, xrayImage1, xrayImage2, xrayImage3, archImage1, archImage2, archImage3, archImage4, archImage5,
            archImage6, archImage7, archImage8)

    }
}