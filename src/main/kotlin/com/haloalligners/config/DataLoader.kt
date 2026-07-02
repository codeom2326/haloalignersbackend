package com.haloalligners.config

import com.haloalligners.model.ClinicAddressDetailsEntity
import com.haloalligners.model.ClinicContactsAndLabPartnersEntity
import com.haloalligners.model.DocumentMetadataEntity
import com.haloalligners.model.DocumentVerificationAndSignatureEntity
import com.haloalligners.model.PractitionerDetailsEntity
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DataLoader(
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        try {
            if (clinicContactsAndLabPartnersRepository.findByUsername("superadmin").isEmpty) {
                val superadmin = ClinicContactsAndLabPartnersEntity(
                    username = "superadmin",
                    password = passwordEncoder.encode("password"),
                    role = "SUPER_ADMIN",
                    email = "superadmin@haloaligners.com",
                    mobile = "1234567890",
                    landLine = null,
                    preferredPartnerCrown = "Crown",
                    preferredPartnerImplants = "Implants",
                    registrationStatus = "Registered"
                )

                val superAdminPractionerDetails = PractitionerDetailsEntity(
                    clinicContactsAndLabPartners = superadmin,
                    fullName = "Super Admin",
                    doctorRegistrationNumber = "1234567890",
                    dateOfApplication = LocalDate.now(),
                    pan = "ABCDE1234F",
                    practitionerCategory = "Private Practitioner",
                    businessArea = "Nobel Biocare"
                )

                val superAdminClinicAddressDetails = ClinicAddressDetailsEntity(
                    clinicContactsAndLabPartners = superadmin,
                    clinicName = "Super Admin Clinic",
                    addressLine1 = "123 Admin St",
                    addressLine2 = "Suite 100",
                    addressLine3 = "Near Clock Tower",
                    addressLine4 = "Main Road",
                    addressLine5 = "Admin City, 12345, State",
                    isDispatchAddressSameAsInvoice = true
                )

                val superAdminDocumentVerificationAndSignatureEntity = DocumentVerificationAndSignatureEntity(
                    clinicContactsAndLabPartners = superadmin,
                    addressProofType = "Passport",
                    addressProofCopy = "passport_copy.jpg",
                    isClinicGstRegistered = false,
                    gstNumber = null,
                    panCard = true,
                    doctorRegistrationCertificate = true,
                    letterHeadOrVisitingCard = true
                )

                val superAdminDocumentMetadata = DocumentMetadataEntity(
                    documentVerificationAndSignature = superAdminDocumentVerificationAndSignatureEntity,
                    addressProofMetadata = "Address proof metadata details",
                    gstMetadata = null,
                    panCardMetadata = "PAN card metadata details",
                    doctorRegistrationCertificateMetadata = "Doctor registration metadata details",
                    letterHeadOrVisitingCardMetadata = "Letter head or visiting card metadata details",
                    signatureAndStampMetadata = "Signature and stamp metadata details"
                )

                // Establish bidirectional relationships
                superadmin.practitionerDetails = superAdminPractionerDetails
                superadmin.clinicAddressDetails = superAdminClinicAddressDetails
                superadmin.documentVerificationAndSignature = superAdminDocumentVerificationAndSignatureEntity
                superAdminDocumentVerificationAndSignatureEntity.documentMetadata = superAdminDocumentMetadata

                // Save only the parent entity; cascading will handle the rest
                clinicContactsAndLabPartnersRepository.save(superadmin)
            }
        } catch (e: EmptyResultDataAccessException) {
            println("No user found")
        }
    }
}