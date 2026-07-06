package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "treatment_stages")
class TreatmentStageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    @JsonBackReference
    val case: CaseEntity,

    @Column(nullable = false)
    val stage: String, // e.g., "XRAY", "Upper Arch"

    @Column(nullable = false)
    val subStage: String, // e.g., "OPG", "Upper_1"

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)