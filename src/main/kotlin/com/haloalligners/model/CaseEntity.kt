package com.haloalligners.model

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
    var status: String = "PENDING_IMAGES", // Initial status

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "case", cascade = [CascadeType.ALL], orphanRemoval = true)
    val treatmentStages: MutableList<TreatmentStageEntity> = mutableListOf()
)