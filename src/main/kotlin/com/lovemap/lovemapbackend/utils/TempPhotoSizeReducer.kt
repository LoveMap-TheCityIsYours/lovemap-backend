package com.lovemap.lovemapbackend.utils

import com.lovemap.lovemapbackend.lovespot.photo.LoveSpotPhotoRepository
import com.lovemap.lovemapbackend.lovespot.photo.PhotoStore
import com.lovemap.lovemapbackend.lovespot.photo.converter.PhotoDownscaler
import com.lovemap.lovemapbackend.lovespot.photo.converter.PhotoDto
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import java.io.ByteArrayOutputStream
import java.net.URL
import javax.imageio.ImageIO

// disabled
class TempPhotoSizeReducer(
    private val photoStore: PhotoStore,
    private val photoDownscaler: PhotoDownscaler,
    private val loveSpotPhotoRepository: LoveSpotPhotoRepository
) : ApplicationRunner {

    private val format = "jpg"

    override fun run(args: ApplicationArguments?) {
        mono {
            loveSpotPhotoRepository.findAll().collect {
                if (it.fileName == "missing") {
                    // https://storage.googleapis.com/download/storage/v1/b/lovemap-photos/o/prod_b05cd640-f76e-4728-9e9c-03fadc9bc1c5.jpg?generation=1672079809524106&alt=media
                    val fileName = it.url.substringAfter("/lovemap-photos/o/").substringBefore("?generation=")
                    it.fileName = fileName
                    loveSpotPhotoRepository.save(it)
                    val bufferedImage = ImageIO.read(URL(it.url))
                    val outputStream = ByteArrayOutputStream()
                    ImageIO.write(bufferedImage, format, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val scaled = photoDownscaler.scaleDown(byteArray)
                    photoStore.delete(it)
                    it.url = photoStore.persist(
                        PhotoDto(
                            fileName = it.fileName,
                            extension = format,
                            byteArray = scaled
                        )
                    )
                    loveSpotPhotoRepository.save(it)
                }
            }

        }.subscribe()
    }

}