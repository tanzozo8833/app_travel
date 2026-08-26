package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import kotlin.math.min

object ImageColorFilterProcessor {

    private const val TAG = "ImageColorProcessor"
    private const val MAX_DIMENSION = 1440 // High quality, crisp square image

    suspend fun processAndSaveImage(
        context: Context,
        imageSource: String,
        colorMode: CheckInColorMode,
        isMirrored: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Decode bitmap with orientation correction
            val decodedBitmap = decodeBitmap(context, imageSource)
                ?: return@withContext Result.failure(Exception("Không thể đọc tệp ảnh nguồn"))

            // 2. Crop to square (1:1 aspect ratio like Locket)
            val squareBitmap = cropToSquare(decodedBitmap)

            // 3. Scale down if necessary to avoid excessive memory usage
            val scaledBitmap = if (squareBitmap.width > MAX_DIMENSION) {
                Bitmap.createScaledBitmap(squareBitmap, MAX_DIMENSION, MAX_DIMENSION, true)
            } else {
                squareBitmap
            }

            // 4. Apply Color Filter and Mirroring if needed
            val processedBitmap = applyFilterAndTransform(
                source = scaledBitmap,
                matrixArray = PhotoFilters.getMatrixForMode(colorMode),
                isMirrored = isMirrored
            )

            // 5. Save to app-private directory (survives gallery deletion, requires no extra permissions)
            val targetDir = File(context.filesDir, "checkin_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "locket_stamp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(targetDir, fileName)

            FileOutputStream(targetFile).use { outStream ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, outStream)
                outStream.flush()
            }

            // Clean up temporary bitmaps if they are different references
            if (decodedBitmap != squareBitmap && !decodedBitmap.isRecycled) {
                decodedBitmap.recycle()
            }
            if (scaledBitmap != squareBitmap && !scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }
            if (processedBitmap != scaledBitmap && !processedBitmap.isRecycled) {
                processedBitmap.recycle()
            }

            val savedUri = Uri.fromFile(targetFile).toString()
            Log.d(TAG, "Processed and saved image to: $savedUri (Filter: ${colorMode.title})")
            Result.success(savedUri)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process image filter", e)
            Result.failure(e)
        }
    }

    private fun decodeBitmap(context: Context, source: String): Bitmap? {
        return try {
            when {
                source.startsWith("content://") -> {
                    val uri = Uri.parse(source)
                    var rotation = 0
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        try {
                            val exif = ExifInterface(stream)
                            rotation = exifToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to read EXIF from stream", e)
                        }
                    }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        if (rotation != 0 && bitmap != null) rotateBitmap(bitmap, rotation.toFloat()) else bitmap
                    }
                }
                source.startsWith("file://") || source.startsWith("/") -> {
                    val filePath = if (source.startsWith("file://")) Uri.parse(source).path ?: source else source
                    var rotation = 0
                    try {
                        val exif = ExifInterface(filePath)
                        rotation = exifToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read EXIF from file: $filePath", e)
                    }
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    if (rotation != 0 && bitmap != null) rotateBitmap(bitmap, rotation.toFloat()) else bitmap
                }
                source.startsWith("http://") || source.startsWith("https://") -> {
                    val url = URL(source)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream.use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }
                else -> {
                    val file = File(source)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding bitmap from source: $source", e)
            null
        }
    }

    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val minDim = min(width, height)
        val cropX = (width - minDim) / 2
        val cropY = (height - minDim) / 2
        return if (cropX == 0 && cropY == 0 && width == height) {
            bitmap
        } else {
            Bitmap.createBitmap(bitmap, cropX, cropY, minDim, minDim)
        }
    }

    private fun applyFilterAndTransform(
        source: Bitmap,
        matrixArray: FloatArray?,
        isMirrored: Boolean
    ): Bitmap {
        if (matrixArray == null && !isMirrored) {
            return source
        }

        val resultBitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        if (matrixArray != null) {
            val colorMatrix = ColorMatrix(matrixArray)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        val matrix = Matrix()
        if (isMirrored) {
            matrix.preScale(-1f, 1f, source.width / 2f, source.height / 2f)
        }

        canvas.drawBitmap(source, matrix, paint)
        return resultBitmap
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun exifToDegrees(exifOrientation: Int): Int = when (exifOrientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        else -> 0
    }
}
