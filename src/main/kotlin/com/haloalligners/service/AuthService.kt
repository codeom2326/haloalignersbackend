package com.haloalligners.service

import com.haloalligners.controller.AuthRequest
import com.haloalligners.controller.LoginRequest
import com.haloalligners.controller.LoginResponse
import com.haloalligners.dto.ApiResponse
import com.haloalligners.dto.GetSingleUserResponse
import com.haloalligners.dto.GetUsersResponse
import com.haloalligners.exception.DuplicateValueException
import com.haloalligners.model.*
import com.haloalligners.repository.ClinicContactsAndLabPartnersRepository
import com.haloalligners.repository.PractitionerDetailsRepository
import com.haloalligners.repository.RejectedUserRepository
import com.haloalligners.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
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
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

@Service
class AuthService(
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository,
    private val practitionerDetailsRepository: PractitionerDetailsRepository,
    private val rejectedUserRepository: RejectedUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
    private val cloudinaryService: CloudinaryService
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional
    @CacheEvict(value = ["users"], allEntries = true)
    fun registerNewUser(
        request: AuthRequest,
        photo: MultipartFile,
        addressProof: MultipartFile?,
        gstCertificate: MultipartFile?,
        panFile: MultipartFile?,
        registrationCertificate: MultipartFile?,
        letterheadOrVisitingCard: MultipartFile?,
        signatureOrStamp: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
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

        val photoUrl: String
        val addressProofUrl: String?
        val gstCertificateUrl: String?
        val panUrl: String?
        val registrationUrl: String?
        val letterheadOrVisitingCardUrl: String?
        val signatureOrStampUrl: String?

        try {
            Executors.newVirtualThreadPerTaskExecutor().use { executor ->
                val photoFuture = CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(photo) }, executor)
                val addressProofFuture = addressProof?.let { f -> CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(f) }, executor) }
                val gstCertificateFuture = gstCertificate?.let { f -> CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(f) }, executor) }
                val panFuture = panFile?.let { f -> CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(f) }, executor) }
                val registrationFuture = registrationCertificate?.let { f -> CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(f) }, executor) }
                val letterheadOrVisitingCardFuture = letterheadOrVisitingCard?.let { f -> CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(f) }, executor) }
                val signatureOrStampFuture = signatureOrStamp?.let { f -> CompletableFuture.supplyAsync({ cloudinaryService.uploadFile(f) }, executor) }

                val allFutures = listOfNotNull(
                    photoFuture,
                    addressProofFuture,
                    gstCertificateFuture,
                    panFuture,
                    registrationFuture,
                    letterheadOrVisitingCardFuture,
                    signatureOrStampFuture
                ).toTypedArray()

                CompletableFuture.allOf(*allFutures).get(120, TimeUnit.SECONDS)

                photoUrl = photoFuture.join()
                addressProofUrl = addressProofFuture?.join()
                gstCertificateUrl = gstCertificateFuture?.join()
                panUrl = panFuture?.join()
                registrationUrl = registrationFuture?.join()
                letterheadOrVisitingCardUrl = letterheadOrVisitingCardFuture?.join()
                signatureOrStampUrl = signatureOrStampFuture?.join()
            }
        } catch (e: Exception) {
            logger.error("File upload failed during registration.", e)
            throw IllegalStateException("File upload failed during registration. Please try again.", e)
        }


        val superAdminDocumentMetadata = DocumentMetadataEntity(
            documentVerificationAndSignature = userDocumentVerificationAndSignatureEntity,
            addressProofMetadata = addressProofUrl ?: "N/A",
            gstMetadata = gstCertificateUrl,
            panCardMetadata = panUrl,
            doctorRegistrationCertificateMetadata = registrationUrl,
            letterHeadOrVisitingCardMetadata = letterheadOrVisitingCardUrl ?: "N/A",
            signatureAndStampMetadata = signatureOrStampUrl ?: "N/A",
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

    @Cacheable("users")
    fun getUsers(requestStatus: String?): List<GetUsersResponse>{
        if (requestStatus.equals("REJECTED", ignoreCase = true)) {
            return rejectedUserRepository.findAll().map {
                GetUsersResponse(
                    id = it.originalUserId,
                    username = it.username,
                    role = "USER", // Rejected users are always users
                    email = it.email,
                    landLine = null, // Not stored in rejection table
                    mobile = it.mobile,
                    preferredPartnerCrown = null, // Not stored
                    preferredPartnerImplants = null, // Not stored
                    registrationStatus = "REJECTED",
                    registrationDate = it.rejectionDate.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                )
            }
        }

        val allUsers = if (requestStatus.isNullOrBlank()) {
            clinicContactsAndLabPartnersRepository.findAll()
        } else {
            clinicContactsAndLabPartnersRepository.findAll().filter { it.registrationStatus.equals(requestStatus, ignoreCase = true) }
        }

        return allUsers.filter { it.role != "SUPER_ADMIN" }.map {
            GetUsersResponse(
                id = it.id!!,
                username = it.username,
                role = it.role,
                email = it.email,
                landLine = it.landLine,
                mobile = it.mobile,
                preferredPartnerCrown = it.preferredPartnerCrown,
                preferredPartnerImplants = it.preferredPartnerImplants,
                registrationStatus = it.registrationStatus,
                registrationDate = it.registrationDate
            )
        }
    }

    fun getUsersByCaseStatus(status: String): List<GetUsersResponse> {
        return clinicContactsAndLabPartnersRepository.findByCasesStatus(status).map {
            GetUsersResponse(
                id = it.id!!,
                username = it.username,
                role = it.role,
                email = it.email,
                landLine = it.landLine,
                mobile = it.mobile,
                preferredPartnerCrown = it.preferredPartnerCrown,
                preferredPartnerImplants = it.preferredPartnerImplants,
                registrationStatus = it.registrationStatus,
                registrationDate = it.registrationDate
            )
        }
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
            registrationDate = user.registrationDate,
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

    @Transactional
    @CacheEvict(value = ["users"], allEntries = true)
    fun updateUser(
        id: Long,
        request: AuthRequest,
        photo: MultipartFile?,
        addressProof: MultipartFile?,
        gstCertificate: MultipartFile?,
        panFile: MultipartFile?,
        registrationCertificate: MultipartFile?,
        letterheadOrVisitingCard: MultipartFile?,
        signatureOrStamp: MultipartFile?
    ): ResponseEntity<ApiResponse<Unit>> {
        val user = clinicContactsAndLabPartnersRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User with ID $id not found") }

        user.username = request.username
        user.password = passwordEncoder.encode(request.password)
        user.landLine = request.landLine
        user.mobile = request.mobile
        user.email = request.email
        user.preferredPartnerCrown = request.preferredPartnerCrown
        user.preferredPartnerImplants = request.preferredPartnerImplants

        val practitionerDetails = user.practitionerDetails!!
        practitionerDetails.fullName = request.fullName
        practitionerDetails.doctorRegistrationNumber = request.doctorRegistrationNumber
        practitionerDetails.pan = request.pan
        practitionerDetails.practitionerCategory = request.practitionerCategory
        practitionerDetails.businessArea = request.businessArea

        val clinicAddressDetails = user.clinicAddressDetails!!
        clinicAddressDetails.clinicName = request.clinicName
        clinicAddressDetails.addressLine1 = request.addressLine1
        clinicAddressDetails.addressLine2 = request.addressLine2
        clinicAddressDetails.addressLine3 = request.addressLine3
        clinicAddressDetails.addressLine4 = request.addressLine4
        clinicAddressDetails.addressLine5 = request.addressLine5
        clinicAddressDetails.isDispatchAddressSameAsInvoice = request.isDispatchAddressSameAsInvoice

        val documentVerificationAndSignature = user.documentVerificationAndSignature!!
        documentVerificationAndSignature.addressProofType = request.addressProofType
        documentVerificationAndSignature.isClinicGstRegistered = request.isClinicGstRegistered
        documentVerificationAndSignature.gstNumber = request.gstNumber
        documentVerificationAndSignature.panCard = request.panCard
        documentVerificationAndSignature.doctorRegistrationCertificate = request.doctorRegistrationCertificate
        documentVerificationAndSignature.letterHeadOrVisitingCard = request.letterHeadOrVisitingCard

        val documentMetadata = documentVerificationAndSignature.documentMetadata!!

        photo?.let { documentMetadata.photoMetadata = cloudinaryService.updateFile(documentMetadata.photoMetadata, it) }
        addressProof?.let { documentMetadata.addressProofMetadata = cloudinaryService.updateFile(documentMetadata.addressProofMetadata, it) }
        gstCertificate?.let { documentMetadata.gstMetadata = cloudinaryService.updateFile(documentMetadata.gstMetadata, it) }
        panFile?.let { documentMetadata.panCardMetadata = cloudinaryService.updateFile(documentMetadata.panCardMetadata, it) }
        registrationCertificate?.let { documentMetadata.doctorRegistrationCertificateMetadata = cloudinaryService.updateFile(documentMetadata.doctorRegistrationCertificateMetadata, it) }
        letterheadOrVisitingCard?.let { documentMetadata.letterHeadOrVisitingCardMetadata = cloudinaryService.updateFile(documentMetadata.letterHeadOrVisitingCardMetadata, it) }
        signatureOrStamp?.let { documentMetadata.signatureAndStampMetadata = cloudinaryService.updateFile(documentMetadata.signatureAndStampMetadata, it) }

        clinicContactsAndLabPartnersRepository.save(user)

        val response = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "User with ID $id updated successfully.",
            data = null
        )
        return ResponseEntity.ok(response)
    }

    @Transactional
    @CacheEvict(value = ["users"], allEntries = true)
    fun updateUserStatus(id: Long, status: String, reason: String?): ResponseEntity<ApiResponse<Unit>> {
        val user = clinicContactsAndLabPartnersRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User with ID $id not found") }

        if (status.equals("REJECTED", ignoreCase = true)) {
            val rejectedUser = RejectedUserEntity(
                originalUserId = user.id!!,
                username = user.username,
                email = user.email,
                mobile = user.mobile,
                fullName = user.practitionerDetails?.fullName ?: "N/A",
                pan = user.practitionerDetails?.pan ?: "N/A",
                clinicName = user.clinicAddressDetails?.clinicName ?: "N/A",
                rejectionReason = reason
            )
            rejectedUserRepository.save(rejectedUser)
            clinicContactsAndLabPartnersRepository.delete(user)
            
            val response = ApiResponse<Unit>(
                status = HttpStatus.OK.value(),
                message = "User with ID $id has been rejected and moved to the rejection table.",
                data = null
            )
            return ResponseEntity.ok(response)
        } else {
            user.registrationStatus = status
            clinicContactsAndLabPartnersRepository.save(user)
            val response = ApiResponse<Unit>(
                status = HttpStatus.OK.value(),
                message = "User status updated successfully to '$status'.",
                data = null
            )
            return ResponseEntity.ok(response)
        }
    }

    @CacheEvict(value = ["users"], allEntries = true)
    @Transactional
    fun deleteUser(id: Long): ResponseEntity<ApiResponse<Unit>> {
        val user = clinicContactsAndLabPartnersRepository.findById(id)
            .orElseThrow { UsernameNotFoundException("User with ID $id not found") }

        val documentMetadata = user.documentVerificationAndSignature?.documentMetadata
        documentMetadata?.let {
            cloudinaryService.deleteFile(it.photoMetadata)
            cloudinaryService.deleteFile(it.addressProofMetadata)
            it.gstMetadata?.let { gst -> cloudinaryService.deleteFile(gst) }
            it.panCardMetadata?.let { pan -> cloudinaryService.deleteFile(pan) }
            it.doctorRegistrationCertificateMetadata?.let { cert -> cloudinaryService.deleteFile(cert) }
            cloudinaryService.deleteFile(it.letterHeadOrVisitingCardMetadata)
            cloudinaryService.deleteFile(it.signatureAndStampMetadata)
        }

        clinicContactsAndLabPartnersRepository.deleteById(id)
        val response = ApiResponse<Unit>(
            status = HttpStatus.OK.value(),
            message = "User with ID $id deleted successfully.",
            data = null
        )
        return ResponseEntity.ok(response)
    }
}