package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.haloalligners.constant.AppConstants
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "rejected_cases")
class RejectedCaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val username: String,

    @Column(nullable = false)
    var caseName: String,

    @Column(nullable = false)
    val patientName: String,

    @Column(nullable = false)
    val patientAge: Int,

    @Column(nullable = false)
    val patientGender: String,

    @Column(nullable = true, length = 1000)
    val existingDisease: String?,

    @Column(nullable = false)
    var status: String = AppConstants.CASE_REJECTED, // Initial status

    @Column(nullable = false)
    val rejectedAt: Instant = Instant.now()
)