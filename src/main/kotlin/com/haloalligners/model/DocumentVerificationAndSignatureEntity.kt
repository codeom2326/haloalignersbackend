package com.haloalligners.model

import jakarta.persistence.*

@Entity
@Table(name = "document_verification_signature")
class DocumentVerificationAndSignatureEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "clinic_contacts_and_lab_partners_id", referencedColumnName = "id")
    val clinicContactsAndLabPartners: ClinicContactsAndLabPartnersEntity,

    @Column(nullable = false)
    val addressProofType: String = "",

    @Column(nullable = false)
    val addressProofCopy: String = "",

    @Column(nullable = false)
    val isClinicGstRegistered: Boolean = false,

    @Column
    val gstNumber: String? = null,

    @Column
    val panCard: Boolean? = false,

    @Column
    val doctorRegistrationCertificate: Boolean? = false,

    @Column
    val letterHeadOrVisitingCard: Boolean? = false,

    @OneToOne(mappedBy = "documentVerificationAndSignature", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var documentMetadata: DocumentMetadataEntity? = null
)