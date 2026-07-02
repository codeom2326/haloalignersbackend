package com.haloalligners.service

import com.haloalligners.controller.AuthRequest
import com.haloalligners.controller.LoginRequest
import com.haloalligners.controller.LoginResponse
import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.GetUserResponse
import com.haloalligners.model.ClinicAddressDetailsEntity
import com.haloalligners.model.ClinicContactsAndLabPartnersEntity
import com.haloalligners.model.DocumentMetadataEntity
import com.haloalligners.model.DocumentVerificationAndSignatureEntity
import com.haloalligners.model.PractitionerDetailsEntity
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import com.haloalligners.security.JwtService
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

@Service
class AuthService(
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository,
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
        if (clinicContactsAndLabPartnersRepository.findByUsername(request.username).isPresent) {
            val response = ApiResponse<Unit>(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Username already exists",
                data = null
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
        }
        if (clinicContactsAndLabPartnersRepository.findByEmail(request.email).isPresent) {
            val response = ApiResponse<Unit>(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Email already exists",
                data = null
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response)
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

        clinicContactsAndLabPartnersRepository.save(newUser)
        
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

    fun getUsers(): List<GetUserResponse>{
        val allUsers = clinicContactsAndLabPartnersRepository.findAll()
        val usersResponseList = mutableListOf<GetUserResponse>()
        allUsers.forEach {
            usersResponseList.add(
                GetUserResponse(
                    username = it.username,
                    role = it.role,
                    email = it.email,
                    landLine = it.landLine,
                    mobile = it.mobile,
                    preferredPartnerCrown = it.preferredPartnerCrown,
                    preferredPartnerImplants = it.preferredPartnerImplants,
                    registrationStatus = it.registrationStatus,
                    fullName = it.practitionerDetails!!.fullName,
                    doctorRegistrationNumber = it.practitionerDetails!!.doctorRegistrationNumber,
                    dateOfApplication = it.practitionerDetails!!.dateOfApplication,
                    pan = it.practitionerDetails!!.pan,
                    practitionerCategory = it.practitionerDetails!!.practitionerCategory,
                    businessArea = it.practitionerDetails!!.businessArea,
                    clinicName = it.clinicAddressDetails!!.clinicName,
                    addressLine1 = it.clinicAddressDetails!!.addressLine1,
                    addressLine2 = it.clinicAddressDetails!!.addressLine2,
                    addressLine3 = it.clinicAddressDetails!!.addressLine3,
                    addressLine4 = it.clinicAddressDetails!!.addressLine4,
                    addressLine5 = it.clinicAddressDetails!!.addressLine5,
                    isDispatchAddressSameAsInvoice = it.clinicAddressDetails!!.isDispatchAddressSameAsInvoice,
                    addressProofType = it.documentVerificationAndSignature!!.addressProofType,
                    addressProofCopy = it.documentVerificationAndSignature!!.addressProofCopy,
                    isClinicGstRegistered = it.documentVerificationAndSignature!!.isClinicGstRegistered,
                    gstNumber = it.documentVerificationAndSignature!!.gstNumber,
                    panCard = it.documentVerificationAndSignature!!.panCard,
                    doctorRegistrationCertificate = it.documentVerificationAndSignature!!.doctorRegistrationCertificate,
                    letterHeadOrVisitingCard = it.documentVerificationAndSignature!!.letterHeadOrVisitingCard,
                    addressProofMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.addressProofMetadata,
                    gstMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.gstMetadata,
                    panCardMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.panCardMetadata,
                    doctorRegistrationCertificateMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.doctorRegistrationCertificateMetadata,
                    letterHeadOrVisitingCardMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.letterHeadOrVisitingCardMetadata,
                    signatureAndStampMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.signatureAndStampMetadata,
                    photoMetadata = it.documentVerificationAndSignature!!.documentMetadata!!.photoMetadata
                )
            )
        }
        return usersResponseList
    }
}