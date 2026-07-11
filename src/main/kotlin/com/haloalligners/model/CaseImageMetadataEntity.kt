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

    @Column
    val xrayImage1Url: String,

    @Column
    val xrayImage2Url: String,

    @Column
    val xrayImage3Url: String,

    @Column
    val archImage1Url: String,

    @Column
    val archImage2Url: String,

    @Column
    val archImage3Url: String,

    @Column
    val archImage4Url: String,

    @Column
    val archImage5Url: String,

    @Column
    val archImage6Url: String,

    @Column
    val archImage7Url: String,

    @Column
    val archImage8Url: String,

    @Column
    val profileImage1Url: String,

    @Column
    val profileImage2Url: String,

    @Column
    val profileImage3Url: String,

    @Column
    val profileImage4Url: String

)