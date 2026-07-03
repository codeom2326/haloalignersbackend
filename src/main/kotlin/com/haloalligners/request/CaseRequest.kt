package com.haloalligners.request

data class CaseRequest(
    val doctorUsername: String,
    val doctorName: String,
    val patientName: String,
    val patientAge: Int,
    val patientSex: String,
    val preExistingDesease: String
)
