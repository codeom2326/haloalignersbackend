package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "document_metadata")
class DocumentMetadataEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "document_verification_id", referencedColumnName = "id")
    @JsonBackReference
    var documentVerificationAndSignature: DocumentVerificationAndSignatureEntity,

    @Column(nullable = false)
    var addressProofMetadata: String,

    @Column
    var gstMetadata: String?,

    @Column
    var panCardMetadata: String?,

    @Column
    var doctorRegistrationCertificateMetadata: String?,

    @Column
    var letterHeadOrVisitingCardMetadata: String,

    @Column(nullable = false)
    var signatureAndStampMetadata: String,

    @Column(nullable = false)
    var photoMetadata: String
)