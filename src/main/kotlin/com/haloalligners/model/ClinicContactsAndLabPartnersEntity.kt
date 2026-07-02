package com.haloalligners.model

import jakarta.persistence.*

@Entity
@Table(name = "clinic_contacts_lab_partners")
class ClinicContactsAndLabPartnersEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false)
    val username: String, // Renamed from userName and made unique/not nullable

    @Column(nullable = false)
    val password: String?,

    @Column(nullable = false)
    val role: String = "USER", // Added role field

    @Column(unique = true, nullable = false)
    val email: String,

    @Column
    val landLine: String?,

    @Column(nullable = false)
    val mobile: String,

    @Column
    val preferredPartnerCrown: String?,

    @Column
    val preferredPartnerImplants: String?,

    @Column
    val registrationStatus: String,

    // OneToOne relationships previously in UserEntity
    @OneToOne(mappedBy = "clinicContactsAndLabPartners", cascade = [CascadeType.ALL], orphanRemoval = true)
    var practitionerDetails: PractitionerDetailsEntity? = null,

    @OneToOne(mappedBy = "clinicContactsAndLabPartners", cascade = [CascadeType.ALL], orphanRemoval = true)
    var clinicAddressDetails: ClinicAddressDetailsEntity? = null,

    @OneToOne(mappedBy = "clinicContactsAndLabPartners", cascade = [CascadeType.ALL], orphanRemoval = true)
    var documentVerificationAndSignature: DocumentVerificationAndSignatureEntity? = null
)