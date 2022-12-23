package com.lovemap.lovemapbackend.lovespot.photo

import com.lovemap.lovemapbackend.utils.ErrorCode
import com.lovemap.lovemapbackend.utils.LoveMapException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import java.util.*

@Component
class LoveSpotPhotoConverter(
    @Value("\${lovemap.lovespot.photos.supportedFormats}") private val supportedFormats: Set<String>
) {
    suspend fun toPhotoDto(filePart: FilePart): PhotoDto {
        val extension = filePart.filename().substringAfterLast(".").lowercase()
        return PhotoDto(
            fileName = UUID.randomUUID().toString() + ".$extension",
            extension = extension,
            byteArray = toByteArray(filePart)
        )
    }

    private suspend fun toByteArray(filePart: FilePart): ByteArray {
        return DataBufferUtils.join(filePart.content())
            .map { it.asByteBuffer().array() }
            .awaitSingle()
    }

    fun convertEncoding(photoDto: PhotoDto): PhotoDto {
        if (supportedFormats.none { it.equals(photoDto.extension, true) }) {
            throw LoveMapException(HttpStatus.BAD_REQUEST, ErrorCode.UnsupportedImageFormat)
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