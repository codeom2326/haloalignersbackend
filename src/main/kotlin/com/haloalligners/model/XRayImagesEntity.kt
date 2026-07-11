package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "case_xray_images")
class XRayImagesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", referencedColumnName = "id")
    @JsonBackReference
    var case: CaseEntity,

    @Column(nullable = true)
    var xrayImage1Url: String? = null,

    @Column(nullable = true)
    var xrayImage2Url: String? = null,

    @Column(nullable = true)
    var xrayImage3Url: String? = null
)