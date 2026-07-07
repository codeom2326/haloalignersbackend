package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "practitioner_details")
class PractitionerDetailsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "clinic_contacts_and_lab_partners_id", referencedColumnName = "id")
    @JsonBackReference
    var clinicContactsAndLabPartners: ClinicContactsAndLabPartnersEntity,

    @Column(nullable = false)
    var fullName: String = "",

    @Column(unique = true, nullable = false)
    var doctorRegistrationNumber: String = "",

    @Column(nullable = false)
    var dateOfApplication: LocalDate = LocalDate.now(),

    @Column(unique = true, nullable = false)
    var pan: String = "",

    @Column(nullable = false)
    var practitionerCategory: String = "Private Practitioner",

    @Column(nullable = false)
    var businessArea: String = "Nobel Biocare"
)