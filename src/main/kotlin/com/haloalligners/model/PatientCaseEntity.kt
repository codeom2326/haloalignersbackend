package com.haloalligners.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "patient_case")
data class PatientCaseEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val doctorUsername: String,

    @Column(nullable = false)
    val doctorName: String,

    @Column(nullable = false)
    val patientName: String,

    @Column(nullable = false)
    val patientAge: Int,

    @Column(nullable = false)
    val patientSex: String,

    @Column(nullable = false)
    val preExistingDesease: String,

    @Column(nullable = false)
    val xray1ImageUrl: String,

    @Column(nullable = false)
    val xray2ImageUrl: String,

    @Column(nullable = false)
    val xray3ImageUrl: String,

    @Column(nullable = false)
    val archImage1Url: String,

    @Column(nullable = false)
    val archImage2Url: String,

    @Column(nullable = false)
    val archImage3Url: String,

    @Column(nullable = false)
    val archImage4Url: String,

    @Column(nullable = false)
    val archImage5Url: String,

    @Column(nullable = false)
    val archImage6Url: String,

    @Column(nullable = false)
    val archImage7Url: String,

    @Column(nullable = false)
    val archImage8Url: String,
)