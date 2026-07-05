package com.haloalligners.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.Instant

@Entity
@Table(name = "rejected_users")
class RejectedUserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // --- Original User Data ---
    val originalUserId: Long,
    val username: String,
    val email: String,
    val mobile: String,
    val fullName: String,
    val pan: String,
    val clinicName: String,
    
    // --- Rejection Info ---
    @Column(nullable = false)
    val rejectionDate: Instant = Instant.now(),

    @Column(nullable = true, length = 500)
    val rejectionReason: String?
)