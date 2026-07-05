package com.haloalligners.model

import jakarta.persistence.*

@Entity
@Table(name = "clinic_address_details")
class ClinicAddressDetailsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinColumn(name = "clinic_contacts_and_lab_partners_id", referencedColumnName = "id")
    val clinicContactsAndLabPartners: ClinicContactsAndLabPartnersEntity,

    @Column(nullable = false)
    val clinicName: String = "",

    @Column(nullable = false)
    val addressLine1: String = "",

    @Column
    val addressLine2: String? = null,

    @Column
    val addressLine3: String? = null,

    @Column
    val addressLine4: String? = null,

    @Column(nullable = false)
    val addressLine5: String = "",

    @Column(nullable = false)
    val isDispatchAddressSameAsInvoice: Boolean = false
)