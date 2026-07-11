package com.haloalligners.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "case_images_metadata")
class CaseImageMetadataEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column
    val caseId: Long,

    @Column(nullable = true)
    var xrayImage1Url: String?,

    @Column(nullable = true)
    var xrayImage2Url: String?,

    @Column(nullable = true)
    var xrayImage3Url: String?,

    @Column(nullable = true)
    var archImage1Url: String?,

    @Column(nullable = true)
    var archImage2Url: String?,

    @Column(nullable = true)
    var archImage3Url: String?,

    @Column(nullable = true)
    var archImage4Url: String?,

    @Column(nullable = true)
    var archImage5Url: String?,

    @Column(nullable = true)
    var archImage6Url: String?,

    @Column(nullable = true)
    var archImage7Url: String?,

    @Column(nullable = true)
    var archImage8Url: String?,

    @Column(nullable = true)
    var profileImage1Url: String?,

    @Column(nullable = true)
    var profileImage2Url: String?,

    @Column(nullable = true)
    var profileImage3Url: String?,

    @Column(nullable = true)
    var profileImage4Url: String?

)