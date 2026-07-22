package com.haloalligners.service

import com.haloalligners.constant.AppConstants
import com.haloalligners.controller.CreateCaseRequest
import com.haloalligners.model.*
import com.haloalligners.repository.*
import jakarta.persistence.EntityNotFoundException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.zip.ZipInputStream
import kotlin.collections.forEachIndexed

@Service
class CaseService(
    private val caseRepository: CaseRepository,
    private val rejectedCaseRepository: RejectedCaseRepository,
    private val clinicContactsAndLabPartnersRepository: ClinicContactsAndLabPartnersRepository,
    private val cloudinaryService: CloudinaryService,
    private val xRayImagesRepository: XRayImagesRepository,
    private val profileImagesRepository: ProfileImagesRepository,
    private val archImagesRepository: ArchImagesRepository,
    private val stlImagesRepository: StlImagesRepository
) {
    fun createCase(username: String, request: CreateCaseRequest): CaseEntity {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }

        val newCase = CaseEntity(
            user = user,
            caseName = request.caseName,
            patientName = request.patientName,
            patientAge = request.patientAge,
            patientGender = request.patientGender,
            existingDisease = request.existingDisease,
            status = "DRAFT"
        )
        return caseRepository.save(newCase)
    }

    fun uploadXrayImages(caseId: Long, images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val xRayImages = xRayImagesRepository.findByCaseId(caseId).orElse(XRayImagesEntity(case = caseEntity))
        
        images.forEachIndexed { index, file ->
            file?.let {
                when (index) {
                    0 -> xRayImages?.xrayImage1Url = cloudinaryService.uploadFile(it)
                    1 -> xRayImages?.xrayImage2Url = cloudinaryService.uploadFile(it)
                    2 -> xRayImages?.xrayImage3Url = cloudinaryService.uploadFile(it)
                }
            }
        }
        xRayImagesRepository.save(xRayImages)
        updateCaseStatusIfComplete(caseEntity)
    }

    fun updateXrayImages(caseId: Long, images: List<MultipartFile?>) {
        uploadXrayImages(caseId, images)
    }

    fun uploadProfileImages(caseId: Long,images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val profileImages = profileImagesRepository.findByCaseId(caseId).orElse(ProfileImagesEntity(case = caseEntity))

        images.forEachIndexed { index, file ->
            file?.let {
                when (index) {
                    0 -> profileImages?.profileImage1Url = cloudinaryService.uploadFile(it)
                    1 -> profileImages?.profileImage2Url = cloudinaryService.uploadFile(it)
                    2 -> profileImages?.profileImage3Url = cloudinaryService.uploadFile(it)
                    3 -> profileImages?.profileImage4Url = cloudinaryService.uploadFile(it)
                }
            }
        }
        profileImagesRepository.save(profileImages)
        updateCaseStatusIfComplete(caseEntity)
    }

    fun updateProfileImages(caseId: Long, images: List<MultipartFile?>) {
        uploadProfileImages(caseId, images)
    }

    fun uploadArchImages(caseId: Long, images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val archImages = archImagesRepository.findByCaseId(caseId).orElse(ArchImagesEntity(case = caseEntity))

        images.forEachIndexed { index, file ->
            file?.let {
                when (index) {
                    0 -> archImages?.archImage1Url = cloudinaryService.uploadFile(it)
                    1 -> archImages?.archImage2Url = cloudinaryService.uploadFile(it)
                    2 -> archImages?.archImage3Url = cloudinaryService.uploadFile(it)
                    3 -> archImages?.archImage4Url = cloudinaryService.uploadFile(it)
                    4 -> archImages?.archImage5Url = cloudinaryService.uploadFile(it)
                    5 -> archImages?.archImage6Url = cloudinaryService.uploadFile(it)
                    6 -> archImages?.archImage7Url = cloudinaryService.uploadFile(it)
                    7 -> archImages?.archImage8Url = cloudinaryService.uploadFile(it)
                }
            }
        }
        archImagesRepository.save(archImages)
        updateCaseStatusIfComplete(caseEntity)
    }

    fun updateArchImages(caseId: Long, images: List<MultipartFile?>) {
        uploadArchImages(caseId, images)
    }

    private fun updateCaseStatusIfComplete(caseEntity: CaseEntity) {
        val xrays = xRayImagesRepository.findByCaseId(caseEntity.id!!).orElse(null)
        val profiles = profileImagesRepository.findByCaseId(caseEntity.id).orElse(null)
        val arches = archImagesRepository.findByCaseId(caseEntity.id).orElse(null)
        val stl = stlImagesRepository.findByCaseId(caseEntity.id).orElse(null)

        if (xrays?.xrayImage1Url != null && xrays.xrayImage2Url != null && xrays.xrayImage3Url != null &&
            profiles?.profileImage1Url != null && profiles.profileImage2Url != null && profiles.profileImage3Url != null && profiles.profileImage4Url != null &&
            arches?.archImage1Url != null && arches.archImage2Url != null && arches.archImage3Url != null && arches.archImage4Url != null &&
            arches.archImage5Url != null && arches.archImage6Url != null && arches.archImage7Url != null && arches.archImage8Url != null &&
            stl?.stlUpperImageUrl != null && stl.stlLowerImageUrl != null) {
            caseEntity.status = "Before Start"
            caseRepository.save(caseEntity)
        }
    }

    fun getAllCases(): List<Any> {
        val allCases: MutableList<Any> = mutableListOf<Any>()
        val rejectedCases = rejectedCaseRepository.findAll()
        val otherCases = caseRepository.findAll()
        allCases.addAll(rejectedCases)
        allCases.addAll(otherCases)
        return allCases

    }

    fun getAllCasesForUser(username: String): List<CaseEntity> {
        val user = clinicContactsAndLabPartnersRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
        return caseRepository.findByUserId(user.id!!)
    }

    fun getCaseImages(caseId: Long): Map<String, Any?> {
        val xrays = xRayImagesRepository.findByCaseId(caseId).orElse(null)
        val profiles = profileImagesRepository.findByCaseId(caseId).orElse(null)
        val arches = archImagesRepository.findByCaseId(caseId).orElse(null)
        return mapOf(
            "xrays" to xrays,
            "profiles" to profiles,
            "arches" to arches
        )
    }

    fun getXRayImages(caseId: Long): XRayImagesEntity {
        return xRayImagesRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("X-Ray images not found for case ID $caseId") }
    }

    fun getProfileImages(caseId: Long): ProfileImagesEntity {
        return profileImagesRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("Profile images not found for case ID $caseId") }
    }

    fun getArchImages(caseId: Long): ArchImagesEntity {
        return archImagesRepository.findByCaseId(caseId)
            .orElseThrow { EntityNotFoundException("Arch images not found for case ID $caseId") }
    }

    fun getCasesByStatus(status: String): List<Any> {
        return if(AppConstants.CASE_REJECTED.equals(status, ignoreCase = true)) {
            rejectedCaseRepository.findAll()
        } else {
            caseRepository.findByStatus(status)
        }

    }

    fun getCaseById(id: Long): CaseEntity {
        return caseRepository.findById(id).get()
    }

    fun updateCaseStatus(caseId: Long, newStatus: String): Any {
        val case = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        if(AppConstants.CASE_REJECTED.equals(newStatus, ignoreCase = true)){
            val rejectedCase = RejectedCaseEntity(
                username = case.user.username,
                caseName = case.caseName,
                patientName = case.patientName,
                patientAge = case.patientAge,
                patientGender = case.patientGender,
                existingDisease = case.existingDisease
            )
            caseRepository.deleteById(caseId)
            return rejectedCaseRepository.save(rejectedCase)
        }

        case.status = newStatus
        return caseRepository.save(case)
    }

    fun deleteCase(caseId: Long) {
        val case = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }

        case.xrayImages?.let {
            it.xrayImage1Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.xrayImage2Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.xrayImage3Url?.let { url -> cloudinaryService.deleteFile(url) }
        }
        case.profileImages?.let {
            it.profileImage1Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.profileImage2Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.profileImage3Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.profileImage4Url?.let { url -> cloudinaryService.deleteFile(url) }
        }
        case.archImages?.let {
            it.archImage1Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage2Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage3Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage4Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage5Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage6Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage7Url?.let { url -> cloudinaryService.deleteFile(url) }
            it.archImage8Url?.let { url -> cloudinaryService.deleteFile(url) }
        }
        case.stlImages?.let {
            it.stlUpperImageUrl?.let { url -> cloudinaryService.deleteFile(url) }
            it.stlLowerImageUrl?.let { url -> cloudinaryService.deleteFile(url) }
        }

        caseRepository.delete(case)
    }

    fun uploadStlImages(caseId: Long, images: List<MultipartFile?>) {
        val caseEntity = caseRepository.findById(caseId)
            .orElseThrow { EntityNotFoundException("Case with ID $caseId not found.") }
        val stlImagesEntity = stlImagesRepository.findByCaseId(caseId).orElse(StlImagesEntity(case = caseEntity))

        images.forEachIndexed { index, file ->
            file?.let {
                when (index) {
                    0 -> stlImagesEntity?.stlUpperImageUrl = it.originalFilename+"::"+cloudinaryService.uploadFile(it)
                    1 -> stlImagesEntity?.stlLowerImageUrl = it.originalFilename+"::"+cloudinaryService.uploadFile(it)
                }
            }
        }
        stlImagesRepository.save(stlImagesEntity)
        updateCaseStatusIfComplete(caseEntity)
    }
    fun updateStlImages(caseId: Long, images: List<MultipartFile?>) {
        uploadStlImages(caseId, images)
    }

    fun downloadAndUnzipStlFile(caseId: Long): MutableMap<String, ByteArray> {
        val record = stlImagesRepository.findByCaseId(caseId)
            .orElseThrow { NoSuchElementException("STL record not found with ID: $caseId") }

        val upperRestorationRecord = record.stlUpperImageUrl!!.split("::")
        val lowerRestorationRecord = record.stlLowerImageUrl!!.split("::")

        if(upperRestorationRecord.size<2 || lowerRestorationRecord.size<2){
            throw NoSuchElementException("STL record not found with ID: $caseId")
        }

        val upperRestorationOriginalFileName = upperRestorationRecord[0]
        val lowerRestorationOriginalFileName = lowerRestorationRecord[0]

        try {
            val upperRestorationZipBytes = URI.create(upperRestorationRecord[1]).toURL().readBytes()
            val lowerRestorationZipBytes = URI.create(lowerRestorationRecord[1]).toURL().readBytes()

            val upperRestorationUnzippedBytes = unzipSingleFile(upperRestorationZipBytes)
            val lowerRestorationUnzippedBytes = unzipSingleFile(lowerRestorationZipBytes)

            return mutableMapOf(
                upperRestorationOriginalFileName to upperRestorationUnzippedBytes,
                lowerRestorationOriginalFileName to lowerRestorationUnzippedBytes
            )
        } catch (ex: Exception){
            throw Exception(ex.message)
        }
    }

    private fun unzipSingleFile(zipBytes: ByteArray): ByteArray {
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zipInput ->
            val entry = zipInput.nextEntry
            if (entry != null && !entry.isDirectory) {
                val outputStream = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var len: Int
                while (zipInput.read(buffer).also { len = it } > 0) {
                    outputStream.write(buffer, 0, len)
                }
                zipInput.closeEntry()
                return outputStream.toByteArray()
            }
        }
        throw IllegalStateException("Failed to extract STL file from ZIP archive")
    }
}