package com.haloalligners.service

import com.haloalligners.model.*
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import com.haloalligners.repository.PractitionerDetailsRepository
import com.haloalligners.repository.RejectedUserRepository
import com.haloalligners.security.JwtService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

class AuthServiceTest {

    private lateinit var authService: AuthService
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository = mockk()
    private val practitionerDetailsRepository: PractitionerDetailsRepository = mockk()
    private val rejectedUserRepository: RejectedUserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val authenticationManager: AuthenticationManager = mockk()
    private val userDetailsService: UserDetailsService = mockk()
    private val jwtService: JwtService = mockk()
    private val cloudinaryService: CloudinaryService = mockk()

    @BeforeEach
    fun setUp() {
        authService = AuthService(
            clinicContactsAndLabPartnersRepository,
            practitionerDetailsRepository,
            rejectedUserRepository,
            passwordEncoder,
            authenticationManager,
            userDetailsService,
            jwtService,
            cloudinaryService
        )
    }

    @Test
    fun `deleteUser should delete user and associated files from Cloudinary`() {
        val userId = 1L
        val user = mockk<ClinicContactsAndLabPartnersEntity>()
        val documentVerificationAndSignature = mockk<DocumentVerificationAndSignatureEntity>()
        val documentMetadata = mockk<DocumentMetadataEntity>()

        every { user.id } returns userId
        every { user.documentVerificationAndSignature } returns documentVerificationAndSignature
        every { documentVerificationAndSignature.documentMetadata } returns documentMetadata
        every { documentMetadata.photoMetadata } returns "photo_url"
        every { documentMetadata.addressProofMetadata } returns "address_proof_url"
        every { documentMetadata.gstMetadata } returns "gst_url"
        every { documentMetadata.panCardMetadata } returns "pan_url"
        every { documentMetadata.doctorRegistrationCertificateMetadata } returns "cert_url"
        every { documentMetadata.letterHeadOrVisitingCardMetadata } returns "letterhead_url"
        every { documentMetadata.signatureAndStampMetadata } returns "stamp_url"

        every { clinicContactsAndLabPartnersRepository.findById(userId) } returns Optional.of(user)
        every { cloudinaryService.deleteFile(any()) } just Runs
        every { clinicContactsAndLabPartnersRepository.deleteById(userId) } just Runs

        val response = authService.deleteUser(userId)

        verify(exactly = 1) { cloudinaryService.deleteFile("photo_url") }
        verify(exactly = 1) { cloudinaryService.deleteFile("address_proof_url") }
        verify(exactly = 1) { cloudinaryService.deleteFile("gst_url") }
        verify(exactly = 1) { cloudinaryService.deleteFile("pan_url") }
        verify(exactly = 1) { cloudinaryService.deleteFile("cert_url") }
        verify(exactly = 1) { cloudinaryService.deleteFile("letterhead_url") }
        verify(exactly = 1) { cloudinaryService.deleteFile("stamp_url") }
        verify(exactly = 1) { clinicContactsAndLabPartnersRepository.deleteById(userId) }

        assert(response.statusCode == HttpStatus.OK)
        assert(response.body?.message == "User with ID $userId deleted successfully.")
    }
}