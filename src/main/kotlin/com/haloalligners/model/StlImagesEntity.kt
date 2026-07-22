package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "case_stl_images")
class StlImagesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", referencedColumnName = "id")
    @JsonBackReference
    var case: CaseEntity,

    @Column(nullable = true)
    var stlUpperImageUrl: String? = null,

    @Column(nullable = true)
    var stlLowerImageUrl: String? = null
)