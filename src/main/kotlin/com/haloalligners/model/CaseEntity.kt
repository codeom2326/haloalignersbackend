package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import com.haloalligners.constant.AppConstants
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "cases")
class CaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: ClinicContactsAndLabPartnersEntity,

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
    var status: String = AppConstants.CASE_CREATED, // Initial status

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToOne(mappedBy = "case", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var xrayImages: XRayImagesEntity? = null,

    @OneToOne(mappedBy = "case", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var profileImages: ProfileImagesEntity? = null,

    @OneToOne(mappedBy = "case", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var archImages: ArchImagesEntity? = null
)