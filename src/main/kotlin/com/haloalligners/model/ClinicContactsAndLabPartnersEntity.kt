package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "clinic_contacts_lab_partners")
class ClinicContactsAndLabPartnersEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    var username: String, // Renamed from userName and made unique/not nullable

    @Column(nullable = false)
    var password: String?,

    @Column(nullable = false)
    var role: String = "USER", // Added role field

    @Column(unique = true, nullable = false)
    var email: String,

    @Column
    var landLine: String?,

    @Column(nullable = false)
    var mobile: String,

    @Column
    var preferredPartnerCrown: String?,

    @Column
    var preferredPartnerImplants: String?,

    @Column
    var registrationStatus: String,

    @Column(nullable = false)
    var registrationDate: LocalDate = LocalDate.now(),

    @OneToOne(mappedBy = "clinicContactsAndLabPartners", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var practitionerDetails: PractitionerDetailsEntity? = null,

    @OneToOne(mappedBy = "clinicContactsAndLabPartners", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var clinicAddressDetails: ClinicAddressDetailsEntity? = null,

    @OneToOne(mappedBy = "clinicContactsAndLabPartners", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonManagedReference
    var documentVerificationAndSignature: DocumentVerificationAndSignatureEntity? = null
)