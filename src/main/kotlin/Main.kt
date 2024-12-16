package org.example

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.Scanner
import javax.imageio.ImageIO
import kotlin.math.sqrt

class ImageProcessingTask(private val inputFile: File) : Runnable {
    override fun run() {
        println("Procesando: ${inputFile.name} en hilo: ${Thread.currentThread().name}")
        try {
            val bufferedImage = ImageIO.read(inputFile)
            val bufferImageGray = BufferedImage(bufferedImage.width, bufferedImage.height, BufferedImage.TYPE_INT_RGB)

            pictureToGray(bufferedImage, bufferImageGray)
            val outputFileGray = File(inputFile.parent, inputFile.nameWithoutExtension + "Gray.jpg")
            ImageIO.write(bufferImageGray, "jpg", outputFileGray)

            val bufferedImageBW = sobel(bufferImageGray)
            val outputFileBW = File(inputFile.parent, inputFile.nameWithoutExtension + "BW.jpg")
            ImageIO.write(bufferedImageBW, "jpg", outputFileBW)

            println("Procesamiento completado: ${inputFile.name} en hilo: ${Thread.currentThread().name}")
        } catch (e: Exception) {
            println("Error procesando el archivo ${inputFile.name}: ${e.message}")
        }
    }
}

fun main(args : Array<String>) {

    
    val folderPath = args[1]

    val folder = File(folderPath)
    if (!folder.exists() || !folder.isDirectory) {
        println("La ruta introducida no es una carpeta válida.")
        return
    }

    val imageFiles = folder.listFiles { pictures ->
        pictures.extension.lowercase() == "jpg" || pictures.extension.lowercase() == "jpeg"
    }

    if (imageFiles == null || imageFiles.isEmpty()) {
        println("No se encontraron imágenes con extensiones .jpg o .jpeg en la carpeta.")
        return
    }

    val threads = mutableListOf<Thread>()

    for (inputFile in imageFiles) {
        val task = ImageProcessingTask(inputFile)
        val thread = Thread(task)
        threads.add(thread)
        thread.start()
    }


    while (threads.any { it.isAlive }) {
        println("Esperando que terminen todos los hilos su trabajo...")
        Thread.sleep(1000)
    }

    println("Todos los hilos han terminado. Procesamiento finalizado.")
}
fun sobel(bufferedImageGray: BufferedImage): BufferedImage {
    val gx = arrayOf(
        arrayOf(1,0,-1),
        arrayOf(2,0,-2),
        arrayOf(1,0,-1)
    )
    val gy = arrayOf(
        arrayOf(1,2,1),
        arrayOf(0,0,0),
        arrayOf(-1,-2,-1),
    )

    var bufferedImageBW = BufferedImage(bufferedImageGray.width, bufferedImageGray.height, BufferedImage.TYPE_INT_RGB)

    for (x in 1 until bufferedImageGray.width - 1) {
        for (y in 1 until  bufferedImageGray.height - 1) {
            var sumx = 0
            var sumy = 0

            for (dx in -1 .. 1){
                for (dy in -1 .. 1){
                    val colorPixel = Color(bufferedImageGray.getRGB(x + dx,y + dy))
                    val grayValue = colorPixel.red

                    sumx += gx[1 + dx][1 + dy] * grayValue
                    sumy += gy[1 + dx][1 + dy] * grayValue
                }
            }

            val magnitude= sqrt((sumx * sumx + sumy * sumy).toDouble()).toInt()

            val clampedMagnitude = if (magnitude < 0) 0 else if (magnitude > 255) 255 else magnitude

            val edgeColor = Color(
                clampedMagnitude,
                clampedMagnitude,
                clampedMagnitude
            )

            bufferedImageBW.setRGB(x, y, edgeColor.rgb)

        }

    }

    return bufferedImageBW

}

fun pictureToGray(bufferedImage: BufferedImage, bufferImageGray: BufferedImage) {
    for (x in 0 until bufferedImage.width) {
        for (y in 0 until bufferedImage.height) {
            val colorInt = bufferedImage.getRGB(x, y)
            val color = Color(colorInt)
            val red = color.red
            val green = color.green
            val blue = color.blue

            val gray = (red * 0.3 + green * 0.59 + blue * 0.11).toInt()
            val grayColor = Color(gray, gray, gray)

            bufferImageGray.setRGB(x, y, grayColor.rgb)
        }
    }
}