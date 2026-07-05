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
        // Create superadmin if it doesn't exist
        if (clinicRepository.findByUsername("superadmin").isEmpty) {
            createSuperAdmin()
        }

        // Create test user if it doesn't exist
        if (clinicRepository.findByUsername("mishraom60").isEmpty) {
            createTestUser()
        }
    }

    private fun createSuperAdmin() {
        val superadmin = ClinicContactsAndLabPartnersEntity(
            username = "superadmin",
            password = passwordEncoder.encode("admin123"),
            role = "SUPER_ADMIN",
            email = "superadmin@haloaligners.com",
            mobile = "0000000000",
            landLine = "N/A",
            preferredPartnerCrown = "N/A",
            preferredPartnerImplants = "N/A",
            registrationStatus = "APPROVED"
        )
        val practitionerDetails = PractitionerDetailsEntity(
            clinicContactsAndLabPartners = superadmin,
            fullName = "Super Admin",
            doctorRegistrationNumber = "N/A",
            dateOfApplication = LocalDate.now(),
            pan = "SUPERADMINPAN", // Unique PAN
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
        superadmin.practitionerDetails = practitionerDetails
        superadmin.clinicAddressDetails = clinicAddress
        superadmin.documentVerificationAndSignature = docVerification
        docVerification.documentMetadata = docMetadata
        clinicRepository.save(superadmin)
    }

    private fun createTestUser() {
        val testUser = ClinicContactsAndLabPartnersEntity(
            username = "mishraom60",
            password = passwordEncoder.encode("password"),
            role = "USER",
            email = "mishraom60@example.com",
            mobile = "1111111111",
            landLine = "N/A",
            preferredPartnerCrown = "N/A",
            preferredPartnerImplants = "N/A",
            registrationStatus = "PENDING",
            registrationDate = LocalDate.now()
        )
        val practitionerDetails = PractitionerDetailsEntity(
            clinicContactsAndLabPartners = testUser,
            fullName = "Om Mishra",
            doctorRegistrationNumber = "DOC123",
            dateOfApplication = LocalDate.now(),
            pan = "TESTUSERPAN", // Unique PAN
            practitionerCategory = "Dentist",
            businessArea = "Mumbai"
        )
        val clinicAddress = ClinicAddressDetailsEntity(
            clinicContactsAndLabPartners = testUser,
            clinicName = "Mishra Dental Clinic",
            addressLine1 = "123 Test St",
            addressLine2 = null,
            addressLine3 = null,
            addressLine4 = "Mumbai",
            addressLine5 = "Maharashtra",
            isDispatchAddressSameAsInvoice = true
        )
        val docVerification = DocumentVerificationAndSignatureEntity(
            clinicContactsAndLabPartners = testUser,
            addressProofType = "Aadhar",
            addressProofCopy = "N/A",
            isClinicGstRegistered = false,
            gstNumber = null,
            panCard = true,
            doctorRegistrationCertificate = true,
            letterHeadOrVisitingCard = true
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
        testUser.practitionerDetails = practitionerDetails
        testUser.clinicAddressDetails = clinicAddress
        testUser.documentVerificationAndSignature = docVerification
        docVerification.documentMetadata = docMetadata
        clinicRepository.save(testUser)
    }
}