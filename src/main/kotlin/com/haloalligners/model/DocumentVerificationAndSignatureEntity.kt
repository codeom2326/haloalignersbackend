package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*

@Entity
@Table(name = "document_verification_signature")
class DocumentVerificationAndSignatureEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "clinic_contacts_and_lab_partners_id", referencedColumnName = "id")
    @JsonBackReference
    var clinicContactsAndLabPartners: ClinicContactsAndLabPartnersEntity,

    @Column(nullable = false)
    var addressProofType: String = "",

    @Column(nullable = false)
    var addressProofCopy: String = "",

    @Column(nullable = false)
    var isClinicGstRegistered: Boolean = false,

    @Column
    var gstNumber: String? = null,

    @Column
    var panCard: Boolean? = false,

    @Column
    var doctorRegistrationCertificate: Boolean? = false,

    @Column
    var letterHeadOrVisitingCard: Boolean? = false,

    @OneToOne(mappedBy = "documentVerificationAndSignature", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    var documentMetadata: DocumentMetadataEntity? = null
)