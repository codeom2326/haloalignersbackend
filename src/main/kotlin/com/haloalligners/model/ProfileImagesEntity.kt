package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "case_profile_images")
class ProfileImagesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", referencedColumnName = "id")
    @JsonBackReference
    var case: CaseEntity,

    @Column(nullable = true)
    var profileImage1Url: String? = null,

    @Column(nullable = true)
    var profileImage2Url: String? = null,

    @Column(nullable = true)
    var profileImage3Url: String? = null,

    @Column(nullable = true)
    var profileImage4Url: String? = null
)