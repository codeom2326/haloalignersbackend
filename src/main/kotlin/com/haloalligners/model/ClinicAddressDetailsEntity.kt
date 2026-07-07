package com.haloalligners.model

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*

@Entity
@Table(name = "clinic_address_details")
class ClinicAddressDetailsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "clinic_contacts_and_lab_partners_id", referencedColumnName = "id")
    @JsonBackReference
    var clinicContactsAndLabPartners: ClinicContactsAndLabPartnersEntity,

    @Column(nullable = false)
    var clinicName: String = "",

    @Column(nullable = false)
    var addressLine1: String = "",

    @Column
    var addressLine2: String? = null,

    @Column
    var addressLine3: String? = null,

    @Column
    var addressLine4: String? = null,

    @Column(nullable = false)
    var addressLine5: String = "",

    @Column(nullable = false)
    var isDispatchAddressSameAsInvoice: Boolean = false
)