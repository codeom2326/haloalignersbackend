package com.haloalligners.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "practitioner_details")
class PractitionerDetailsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "clinic_contacts_and_lab_partners_id", referencedColumnName = "id")
    val clinicContactsAndLabPartners: ClinicContactsAndLabPartnersEntity,

    @Column(nullable = false)
    val fullName: String = "",

    @Column(unique = true, nullable = false)
    val doctorRegistrationNumber: String = "",

    @Column(nullable = false)
    val dateOfApplication: LocalDate = LocalDate.now(),

    @Column(unique = true, nullable = false)
    val pan: String = "",

    @Column(nullable = false)
    val practitionerCategory: String = "Private Practitioner",

    @Column(nullable = false)
    val businessArea: String = "Nobel Biocare"
)