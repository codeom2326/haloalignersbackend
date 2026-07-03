package com.haloalligners.config

import com.haloalligners.model.*
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DataLoader(
    private val clinicRepository: ClinicContactsAndLabPartnersRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (clinicRepository.findByUsername("superadmin").isEmpty) {
            // 1. Create the main user entity
            val superadmin = ClinicContactsAndLabPartnersEntity(
                username = "superadmin",
                password = passwordEncoder.encode("admin123"), // Correct password
                role = "SUPER_ADMIN",
                email = "superadmin@haloaligners.com",
                mobile = "0000000000",
                landLine = "N/A",
                preferredPartnerCrown = "N/A",
                preferredPartnerImplants = "N/A",
                registrationStatus = "APPROVED"
            )

            // 2. Create related entities with placeholder data
            val practitionerDetails = PractitionerDetailsEntity(
                clinicContactsAndLabPartners = superadmin,
                fullName = "Super Admin",
                doctorRegistrationNumber = "N/A",
                dateOfApplication = LocalDate.now(),
                pan = "N/A",
                practitionerCategory = "N/A",
                businessArea = "N/A"
            )

            val clinicAddress = ClinicAddressDetailsEntity(
                clinicContactsAndLabPartners = superadmin,
                clinicName = "HaloAligners HQ",
                addressLine1 = "N/A",
                addressLine2 = null,
                addressLine3 = null,
                addressLine4 = "N/A",
                addressLine5 = "N/A",
                isDispatchAddressSameAsInvoice = false
            )

            val docVerification = DocumentVerificationAndSignatureEntity(
                clinicContactsAndLabPartners = superadmin,
                addressProofType = "N/A",
                addressProofCopy = "N/A",
                isClinicGstRegistered = false,
                gstNumber = null,
                panCard = false,
                doctorRegistrationCertificate = false,
                letterHeadOrVisitingCard = false
            )

            val docMetadata = DocumentMetadataEntity(
                documentVerificationAndSignature = docVerification,
                addressProofMetadata = "N/A",
                gstMetadata = null,
                panCardMetadata = "N/A",
                doctorRegistrationCertificateMetadata = "N/A",
                letterHeadOrVisitingCardMetadata = "N/A",
                signatureAndStampMetadata = "N/A",
                photoMetadata = "N/A"
            )

            // 3. Link entities together
            superadmin.practitionerDetails = practitionerDetails
            superadmin.clinicAddressDetails = clinicAddress
            superadmin.documentVerificationAndSignature = docVerification
            docVerification.documentMetadata = docMetadata

            // 4. Save the parent entity
            clinicRepository.save(superadmin)
        }
    }
}