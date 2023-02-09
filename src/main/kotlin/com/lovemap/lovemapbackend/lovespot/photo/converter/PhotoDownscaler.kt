package com.lovemap.lovemapbackend.lovespot.photo.converter

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.sqrt

@Component
class PhotoDownscaler {

    private val logger = KotlinLogging.logger {}

    private val maxPixels = 3000 * 2000
    private val format = "jpg"

    fun supportedFormats() = setOf("jpg", "jpeg")

    fun scaleDown(input: ByteArray): ByteArray {
        val originalSize = input.size / 1024
        val originalImage: BufferedImage = ImageIO.read(ByteArrayInputStream(input))
        val pixels = originalImage.width * originalImage.height

        return if (pixels > maxPixels) {
            scaleDownLargeImage(pixels, originalImage, originalSize)
        } else {
            tryCompressSmallImage(originalImage, originalSize, input)
        }
    }

    private fun scaleDownLargeImage(
        pixels: Int,
        originalImage: BufferedImage,
        originalSize: Int
    ): ByteArray {
        val scaleFactor: Double = sqrt(pixels.toDouble() / maxPixels)
        val targetWidth = (originalImage.width / scaleFactor).toInt()
        val targetHeight = (originalImage.height / scaleFactor).toInt()
        val scaledByteArray = doScale(originalImage, targetWidth, targetHeight)
        val scaledSize = scaledByteArray.size / 1024
        logger.info { "Scaled down image from size $originalSize kB to $scaledSize kB." }

        return scaledByteArray
    }

    private fun tryCompressSmallImage(
        originalImage: BufferedImage,
        originalSize: Int,
        input: ByteArray
    ): ByteArray {
        val targetWidth = originalImage.width
        val targetHeight = originalImage.height
        val scaledByteArray = doScale(originalImage, targetWidth, targetHeight)
        val scaledSize = scaledByteArray.size / 1024
        logger.info { "Scaled down image size $originalSize kB to $scaledSize kB. Returning smaller" }

        return if (scaledSize < originalSize) {
            scaledByteArray
        } else {
            input
        }
    }

    private fun doScale(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): ByteArray {
        val scaleType = Image.SCALE_SMOOTH
        val scaledImage: Image = originalImage.getScaledInstance(
            targetWidth,
            targetHeight,
            scaleType
        )

        val bufferedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
        bufferedImage.graphics.drawImage(scaledImage, 0, 0, null)

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, format, outputStream)

        return outputStream.toByteArray()
    }

}
