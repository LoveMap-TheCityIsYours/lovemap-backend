package com.lovemap.lovemapbackend.lovespot.photo

import com.google.auth.Credentials
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.lovemap.lovemapbackend.configuration.GoogleConfigProperties
import com.lovemap.lovemapbackend.lovespot.photo.converter.PhotoDto
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

interface PhotoStore {
    fun persist(photoDto: PhotoDto): String
    fun delete(photo: LoveSpotPhoto)
}

@Component
class GooglePhotoStore(
    private val googleConfigProperties: GoogleConfigProperties,
    private val googleCredentials: Credentials
) : PhotoStore {
    private val logger = KotlinLogging.logger {}

    override fun persist(photoDto: PhotoDto): String {
        logger.info(
            "Persisting photo '{}' with size {}kB",
            photoDto.fileName,
            photoDto.byteArray.size / 1024
        )
        val storage = StorageOptions.newBuilder()
            .setProjectId(googleConfigProperties.projectId)
            .setCredentials(googleCredentials)
            .build().service
        val blobId = BlobId.of(googleConfigProperties.publicPhotosBucket, photoDto.fileName)
        val blobInfo = BlobInfo.newBuilder(blobId).build()

        // Optional: set a generation-match precondition to avoid potential race
        // conditions and data corruptions. The request returns a 412 error if the
        // preconditions are not met.
        val precondition: Storage.BlobTargetOption =
            if (storage[googleConfigProperties.publicPhotosBucket, photoDto.fileName] == null) {
                // For a target object that does not yet exist, set the DoesNotExist precondition.
                // This will cause the request to fail if the object is created before the request runs.
                Storage.BlobTargetOption.doesNotExist()
            } else {
                // If the destination already exists in your bucket, instead set a generation-match
                // precondition. This will cause the request to fail if the existing object's generation
                // changes before the request runs.
                Storage.BlobTargetOption.generationMatch()
            }

        return try {
            val blob = storage.create(blobInfo, photoDto.byteArray, precondition)
            logger.info("Photo '{}' successfully persisted.", photoDto.fileName)
            blob.asBlobInfo().mediaLink
        } catch (e: Exception) {
            logger.error("Failed to persist photo '{}'.", photoDto.fileName, e)
            throw LoveMapException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ImageUploadFailed)
        }
    }

    override fun delete(photo: LoveSpotPhoto) {
        logger.info("Deleting photo '{}'", photo.fileName)
        val storage = StorageOptions.newBuilder()
            .setProjectId(googleConfigProperties.projectId)
            .setCredentials(googleCredentials)
            .build().service
        val blobId = BlobId.of(googleConfigProperties.publicPhotosBucket, photo.fileName)

        return try {
            val result = storage.delete(blobId)
            logger.info("Photo '{}' deleted with result: {}.", photo.fileName, result)
        } catch (e: Exception) {
            logger.error("Failed to delete photo '{}'.", photo.fileName, e)
            throw LoveMapException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.ImageUploadFailed)
        }
    }

}

