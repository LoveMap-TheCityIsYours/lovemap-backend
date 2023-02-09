package com.lovemap.lovemapbackend.photos

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.math.sqrt
import kotlin.test.assertTrue


class PhotoConverterTest {

    private val maxPixels = 3000 * 2000

    @Test
    @Disabled
    fun convert() {
        val imageName = "2"

        val image: BufferedImage =
            ImageIO.read(File("src/test/resources/$imageName.jpg"))

        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", byteArrayOutputStream)

        val originalByteArray: ByteArray = byteArrayOutputStream.toByteArray()

        val originalSize = originalByteArray.size

        val originalImage: BufferedImage = ImageIO.read(ByteArrayInputStream(originalByteArray))
        val pixels = originalImage.width * originalImage.height
        if (pixels > maxPixels) {
            val scaleFactor: Double = sqrt(pixels.toDouble() / maxPixels)

            val targetWidth = (originalImage.width / scaleFactor).toInt()
            val targetHeight = (originalImage.height / scaleFactor).toInt()
            val scaleType = Image.SCALE_SMOOTH
            val scaledImage: Image = originalImage.getScaledInstance(
                targetWidth,
                targetHeight,
                scaleType
            )

            val bufferedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
            bufferedImage.graphics.drawImage(scaledImage, 0, 0, null)

            val outputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "jpg", outputStream)

            val outputPath = "src/test/resources/${imageName}_$scaleType.jpg"
            ImageIO.write(bufferedImage, "jpg", Path(outputPath).createFile().toFile())

            val convertedImage: ByteArray = outputStream.toByteArray()
            val convertedSize = convertedImage.size

            println("Original: $originalSize")
            println("Converted: $convertedSize")
            assertTrue(convertedSize < originalSize)
        }

        println(originalByteArray.size)
    }

}
