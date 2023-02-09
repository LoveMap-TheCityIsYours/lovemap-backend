package com.lovemap.lovemapbackend.lovespot.photo.converter

import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhoto
import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoResponse
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLikersDislikers
import com.lovemap.lovemapbackend.lovespot.photo.like.PhotoLikersDislikersRepository
import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.reactor.awaitSingle
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import java.util.*

@Component
class LoveSpotPhotoConverter(
    @Value("\${lovemap.lovespot.photos.supportedFormats}") private val supportedFormats: Set<String>,
    private val photoDownscaler: PhotoDownscaler,
    private val environment: Environment,
    private val photoLikersDislikersRepository: PhotoLikersDislikersRepository
) {
    private val logger = KotlinLogging.logger {}

    suspend fun toPhotoResponse(
        loveSpotPhoto: LoveSpotPhoto,
        photoLikersDislikers: PhotoLikersDislikers
    ): LoveSpotPhotoResponse {
        return LoveSpotPhotoResponse.of(
            photo = loveSpotPhoto,
            likers = photoLikersDislikers.getLikers(),
            dislikers = photoLikersDislikers.getDislikers()
        )
    }

    suspend fun toPhotoResponse(loveSpotPhoto: LoveSpotPhoto): LoveSpotPhotoResponse {
        val photoLikersDislikers = photoLikersDislikersRepository.findByPhotoId(loveSpotPhoto.id)
        return LoveSpotPhotoResponse.of(
            photo = loveSpotPhoto,
            likers = photoLikersDislikers?.getLikers() ?: emptySet(),
            dislikers = photoLikersDislikers?.getDislikers() ?: emptySet()
        )
    }

    suspend fun toPhotoDto(filePart: FilePart): PhotoDto {
        val extension = filePart.filename().substringAfterLast(".").lowercase()
        val profile = environment.activeProfiles.firstOrNull() ?: "dev"
        verifyEncoding(extension)
        return PhotoDto(
            fileName = profile + "_" + UUID.randomUUID().toString() + ".$extension",
            extension = extension,
            byteArray = toByteArray(filePart)
        )
    }

    private fun verifyEncoding(extension: String) {
        if (supportedFormats.none { it.equals(extension, true) }) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.UnsupportedImageFormat)
        }
    }

    private suspend fun toByteArray(filePart: FilePart): ByteArray {
        return DataBufferUtils.join(filePart.content())
            .map { it.toByteBuffer().array() }
            .awaitSingle()
    }

    fun convertEncoding(photoDto: PhotoDto): PhotoDto {
        if (photoDownscaler.supportedFormats().contains(photoDto.extension)) {
            val start = System.currentTimeMillis()
            val downScaled = photoDto.copy(
                byteArray = photoDownscaler.scaleDown(photoDto.byteArray)
            )
            logger.info { "Scaled down image in ${System.currentTimeMillis() - start}ms." }
            return downScaled
        }
        return photoDto
    }
}

data class PhotoDto(
    val fileName: String,
    val extension: String,
    val byteArray: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoDto

        if (fileName != other.fileName) return false
        if (extension != other.extension) return false
        if (!byteArray.contentEquals(other.byteArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + extension.hashCode()
        result = 31 * result + byteArray.contentHashCode()
        return result
    }
}
