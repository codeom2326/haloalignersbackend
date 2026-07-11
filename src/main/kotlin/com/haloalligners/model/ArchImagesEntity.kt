package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "case_arch_images")
class ArchImagesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", referencedColumnName = "id")
    @JsonBackReference
    var case: CaseEntity,

    @Column(nullable = true)
    var archImage1Url: String? = null,

    @Column(nullable = true)
    var archImage2Url: String? = null,

    @Column(nullable = true)
    var archImage3Url: String? = null,

    @Column(nullable = true)
    var archImage4Url: String? = null,

    @Column(nullable = true)
    var archImage5Url: String? = null,

    @Column(nullable = true)
    var archImage6Url: String? = null,

    @Column(nullable = true)
    var archImage7Url: String? = null,

    @Column(nullable = true)
    var archImage8Url: String? = null
)