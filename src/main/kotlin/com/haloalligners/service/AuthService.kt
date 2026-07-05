package com.haloalligners.service

import com.haloalligners.controller.AuthRequest
import com.haloalligners.controller.LoginRequest
import com.haloalligners.controller.LoginResponse
import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.GetSingleUserResponse
import com.haloalligners.dto.GetUsersResponse
import com.haloalligners.exception.DuplicateValueException
import com.haloalligners.model.ClinicAddressDetailsEntity
import com.haloalligners.model.ClinicContactsAndLabPartnersEntity
import com.haloalligners.model.DocumentMetadataEntity
import com.haloalligners.model.DocumentVerificationAndSignatureEntity
import com.haloalligners.model.PractitionerDetailsEntity
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import com.haloalligners.repository.PractitionerDetailsRepository
import com.haloalligners.security.JwtService
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.regex.Pattern

@Service
class AuthService(
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository,
    private val practitionerDetailsRepository: PractitionerDetailsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
    private val cloudinaryService: CloudinaryService
) {

    fun registerNewUser(
        request: AuthRequest,
        addressProof: MultipartFile,
        gstCertificate: MultipartFile,
        pan: MultipartFile,
        registrationCertificate: MultipartFile,
        letterheadOrVisitingCard: MultipartFile,
        signatureOrStamp: MultipartFile,
        photo: MultipartFile
    ): ResponseEntity<ApiResponse<Unit>>{
        // Proactive checks
        if (clinicContactsAndLabPartnersRepository.findByUsername(request.username).isPresent) {
            throw DuplicateValueException("Username '${request.username}' already exists.")
        }
        if (clinicContactsAndLabPartnersRepository.findByEmail(request.email).isPresent) {
            throw DuplicateValueException("Email '${request.email}' already exists.")
        }
        if (practitionerDetailsRepository.findByPan(request.pan).isPresent) {
            throw DuplicateValueException("PAN '${request.pan}' already exists.")
        }

        val newUser = ClinicContactsAndLabPartnersEntity(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            role = "USER",
            landLine = request.landLine,
            mobile = request.mobile,
            email = request.email,
            preferredPartnerCrown = request.preferredPartnerCrown,
            preferredPartnerImplants = request.preferredPartnerImplants,
            registrationStatus = "Under Approval"
        )
        val userPractionerDetails = PractitionerDetailsEntity(
            clinicContactsAndLabPartners = newUser,
            fullName = request.fullName,
            doctorRegistrationNumber = request.doctorRegistrationNumber,
            dateOfApplication = LocalDate.now(),
            pan = request.pan,
            practitionerCategory = request.practitionerCategory,
            businessArea = request.businessArea
        )

        val userClinicAddressDetails = ClinicAddressDetailsEntity(
            clinicContactsAndLabPartners = newUser,
            clinicName = request.clinicName,
            addressLine1 = request.addressLine1,
            addressLine2 = request.addressLine2,
            addressLine3 = request.addressLine3,
            addressLine4 = request.addressLine4,
            addressLine5 = request.addressLine5,
            isDispatchAddressSameAsInvoice = request.isDispatchAddressSameAsInvoice
        )

        val userDocumentVerificationAndSignatureEntity = DocumentVerificationAndSignatureEntity(
            clinicContactsAndLabPartners = newUser,
            addressProofType = request.addressProofType,
            addressProofCopy = request.addressProofType,
            isClinicGstRegistered = request.isClinicGstRegistered,
            gstNumber = request.gstNumber,
            panCard = request.panCard,
            doctorRegistrationCertificate = request.doctorRegistrationCertificate,
            letterHeadOrVisitingCard = request.letterHeadOrVisitingCard
        )
        val addressProofUrl = cloudinaryService.uploadFile(addressProof)
        val gstCertificateUrl = cloudinaryService.uploadFile(gstCertificate)
        val panUrl = cloudinaryService.uploadFile(pan)
        val registrationUrl = cloudinaryService.uploadFile(registrationCertificate)
        val letterheadOrVisitingCardUrl = cloudinaryService.uploadFile(letterheadOrVisitingCard)
        val signatureOrStampUrl = cloudinaryService.uploadFile(signatureOrStamp)
        val photoUrl = cloudinaryService.uploadFile(photo)


        val superAdminDocumentMetadata = DocumentMetadataEntity(
            documentVerificationAndSignature = userDocumentVerificationAndSignatureEntity,
            addressProofMetadata = addressProofUrl,
            gstMetadata = gstCertificateUrl,
            panCardMetadata = panUrl,
            doctorRegistrationCertificateMetadata = registrationUrl,
            letterHeadOrVisitingCardMetadata = letterheadOrVisitingCardUrl,
            signatureAndStampMetadata = signatureOrStampUrl,
            photoMetadata = photoUrl
        )
        newUser.practitionerDetails = userPractionerDetails
        newUser.clinicAddressDetails = userClinicAddressDetails
        newUser.documentVerificationAndSignature = userDocumentVerificationAndSignatureEntity
        userDocumentVerificationAndSignatureEntity.documentMetadata = superAdminDocumentMetadata

        try {
            clinicContactsAndLabPartnersRepository.save(newUser)
        } catch (e: DataIntegrityViolationException) {
            val rootCause = e.rootCause
            var message = "A database constraint was violated. Please check your input."
            if (rootCause != null && rootCause.message != null) {
                val pattern = Pattern.compile("Detail: Key \\((.*?)\\)=\\((.*?)\\) already exists.")
                val matcher = pattern.matcher(rootCause.message!!)
                if (matcher.find()) {
                    val key = matcher.group(1)
                    message = "The value for the field '$key' already exists. Please use a unique value."
                }
            }
            throw DuplicateValueException(message)
        }
        
        val response = ApiResponse<Unit>(
            status = HttpStatus.CREATED.value(),
            message = "User registered successfully",
            data = null
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    fun login(request: LoginRequest): LoginResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                request.username,
                request.password
            )
        )
        val userDetails: UserDetails = userDetailsService.loadUserByUsername(request.username)
        val token = jwtService.generateToken(userDetails)
        val user = clinicContactsAndLabPartnersRepository.findByUsername(request.username).orElseThrow { UsernameNotFoundException("User not found") }
        val userInfo = com.haloalligners.controller.UserInfo(
            id = user.id!!,
            userName = user.username,
            password = passwordEncoder.encode(user.password),
            role = "USER",
            landLine = user.landLine,
            mobile = user.mobile,
            email = user.email,
            preferredPartnerCrown = user.preferredPartnerCrown,
            preferredPartnerImplants = user.preferredPartnerImplants
        )
        return LoginResponse(token, userInfo)
    }

    fun getUsers(requestStatus: String?): List<GetUsersResponse>{
        val allUsers = if (requestStatus.isNullOrBlank()) {
            clinicContactsAndLabPartnersRepository.findAll()
        } else {
            clinicContactsAndLabPartnersRepository.findAll().filter { it.registrationStatus == requestStatus }
        }

        val usersResponseList = allUsers.filter { it.role != "SUPER_ADMIN" }.map {
            GetUsersResponse(
                id = it.id!!,
                username = it.username,
                role = it.role,
                email = it.email,
                landLine = it.landLine,
                mobile = it.mobile,
                preferredPartnerCrown = it.preferredPartnerCrown,
                preferredPartnerImplants = it.preferredPartnerImplants,
                registrationStatus = it.registrationStatus
            )
        }
        return usersResponseList
    }
    fun getUser(id: Long): GetSingleUserResponse {
        val user = clinicContactsAndLabPartnersRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User not found") }
        return GetSingleUserResponse(
            username = user.username,
            role = user.role,
            email = user.email,
            landLine = user.landLine,
            mobile = user.mobile,
            preferredPartnerCrown = user.preferredPartnerCrown,
            preferredPartnerImplants = user.preferredPartnerImplants,
            registrationStatus = user.registrationStatus,
            fullName = user.practitionerDetails!!.fullName,
            doctorRegistrationNumber = user.practitionerDetails!!.doctorRegistrationNumber,
            dateOfApplication = user.practitionerDetails!!.dateOfApplication,
            pan = user.practitionerDetails!!.pan,
            practitionerCategory = user.practitionerDetails!!.practitionerCategory,
            businessArea = user.practitionerDetails!!.businessArea,
            clinicName = user.clinicAddressDetails!!.clinicName,
            addressLine1 = user.clinicAddressDetails!!.addressLine1,
            addressLine2 = user.clinicAddressDetails!!.addressLine2,
            addressLine3 = user.clinicAddressDetails!!.addressLine3,
            addressLine4 = user.clinicAddressDetails!!.addressLine4,
            addressLine5 = user.clinicAddressDetails!!.addressLine5,
            isDispatchAddressSameAsInvoice = user.clinicAddressDetails!!.isDispatchAddressSameAsInvoice,
            addressProofType = user.documentVerificationAndSignature!!.addressProofType,
            addressProofCopy = user.documentVerificationAndSignature!!.addressProofCopy,
            isClinicGstRegistered = user.documentVerificationAndSignature!!.isClinicGstRegistered,
            gstNumber = user.documentVerificationAndSignature!!.gstNumber,
            panCard = user.documentVerificationAndSignature!!.panCard,
            doctorRegistrationCertificate = user.documentVerificationAndSignature!!.doctorRegistrationCertificate,
            letterHeadOrVisitingCard = user.documentVerificationAndSignature!!.letterHeadOrVisitingCard,
            addressProofMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.addressProofMetadata,
            gstMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.gstMetadata,
            panCardMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.panCardMetadata,
            doctorRegistrationCertificateMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.doctorRegistrationCertificateMetadata,
            letterHeadOrVisitingCardMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.letterHeadOrVisitingCardMetadata,
            signatureAndStampMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.signatureAndStampMetadata,
            photoMetadata = user.documentVerificationAndSignature!!.documentMetadata!!.photoMetadata
        )

    }

    fun updateUserStatus(id: Long, status: String): ResponseEntity<ApiResponse<Unit>> {
        val user = clinicContactsAndLabPartnersRepository.findById(id).orElseThrow { UsernameNotFoundException("User not found") }
        user.registrationStatus = status
        clinicContactsAndLabPartnersRepository.save(user)
        val response = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "User status updated successfully",
            data = null
        )
        return ResponseEntity.status(HttpStatus.OK).body(response)

    }
}