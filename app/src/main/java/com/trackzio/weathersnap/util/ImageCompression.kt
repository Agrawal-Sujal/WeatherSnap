package com.trackzio.weathersnap.util
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageCompressor @Inject constructor() {

    data class CompressionResult(
        val compressedFile: File,
        val originalSizeKb: Long,
        val compressedSizeKb: Long
    )

    fun compress(context: Context, sourceFile: File, quality: Int = 30): CompressionResult {
        val originalSizeKb = sourceFile.length() / 1024

        val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            ?: return CompressionResult(sourceFile, originalSizeKb, originalSizeKb)

        // Scale down if very large
        val scaledBitmap = scaleBitmap(bitmap, 1280)

        val compressedFile = File(
            context.filesDir,
            "images/compressed_${System.currentTimeMillis()}.jpg"
        )
        compressedFile.parentFile?.mkdirs()

        FileOutputStream(compressedFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }

        if (scaledBitmap != bitmap) scaledBitmap.recycle()
        bitmap.recycle()

        val compressedSizeKb = compressedFile.length() / 1024
        return CompressionResult(compressedFile, originalSizeKb, compressedSizeKb)
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / maxOf(width, height)
        return Bitmap.createScaledBitmap(
            bitmap,
            (width * scale).toInt(),
            (height * scale).toInt(),
            true
        )
    }
}