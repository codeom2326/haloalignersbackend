package com.haloalligners.model

import jakarta.persistence.*

@Entity
@Table(name = "document_metadata")
class DocumentMetadataEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "document_verification_id", referencedColumnName = "id")
    val documentVerificationAndSignature: DocumentVerificationAndSignatureEntity,

    @Column(nullable = false)
    val addressProofMetadata: String,

    @Column
    val gstMetadata: String?,

    @Column
    val panCardMetadata: String?,

    @Column
    val doctorRegistrationCertificateMetadata: String?,

    @Column
    val letterHeadOrVisitingCardMetadata: String,

    @Column(nullable = false)
    val signatureAndStampMetadata: String
)