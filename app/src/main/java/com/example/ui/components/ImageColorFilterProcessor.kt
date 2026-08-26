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
import kotlin.math.max
import kotlin.math.min

object ImageColorFilterProcessor {

    private const val TAG = "ImageColorProcessor"
    private const val PRIMARY_TARGET_DIMENSION = 1440 // High quality, crisp square image (Stage 1)
    private const val FALLBACK_TARGET_DIMENSION = 720  // Memory-safe retry dimension (Stage 2)

    /**
     * Process, crop to 1:1, apply color filter, and save to app-private directory.
     * Guaranteed not to crash on OutOfMemoryError, with automatic downsampling on load
     * and fallback retry.
     */
    suspend fun processAndSaveImage(
        context: Context,
        imageSource: String,
        colorMode: CheckInColorMode,
        isMirrored: Boolean = false
    ): Result<String> = withContext(Dispatchers.IO) {
        // Attempt 1: High Quality target dimension (1440px)
        val attempt1 = tryProcessPipeline(context, imageSource, colorMode, isMirrored, PRIMARY_TARGET_DIMENSION)
        if (attempt1.isSuccess) {
            return@withContext attempt1
        }

        val firstError = attempt1.exceptionOrNull()
        Log.w(TAG, "Primary processing failed (${firstError?.javaClass?.simpleName}: ${firstError?.message}). Retrying with fallback target ($FALLBACK_TARGET_DIMENSION px)...")

        // Force GC to free up memory before retry
        System.gc()

        // Attempt 2: Fallback target dimension (720px)
        val attempt2 = tryProcessPipeline(context, imageSource, colorMode, isMirrored, FALLBACK_TARGET_DIMENSION)
        if (attempt2.isSuccess) {
            Log.d(TAG, "Fallback processing succeeded at $FALLBACK_TARGET_DIMENSION px")
            return@withContext attempt2
        }

        Log.e(TAG, "Both processing attempts failed", attempt2.exceptionOrNull())
        Result.failure(attempt2.exceptionOrNull() ?: Exception("Không thể xử lý ảnh"))
    }

    private fun tryProcessPipeline(
        context: Context,
        imageSource: String,
        colorMode: CheckInColorMode,
        isMirrored: Boolean,
        targetDimension: Int
    ): Result<String> {
        var decodedBitmap: Bitmap? = null
        var squareBitmap: Bitmap? = null
        var processedBitmap: Bitmap? = null

        return try {
            // 1. Decode bitmap with inSampleSize calculated BEFORE decoding into memory
            decodedBitmap = decodeSampledBitmap(context, imageSource, targetDimension)
                ?: return Result.failure(Exception("Không thể đọc tệp ảnh nguồn"))

            // 2. Crop to square (1:1 aspect ratio)
            squareBitmap = cropToSquare(decodedBitmap)

            // 3. Apply Color Filter and Mirroring
            processedBitmap = applyFilterAndTransform(
                source = squareBitmap,
                matrixArray = PhotoFilters.getMatrixForMode(colorMode),
                isMirrored = isMirrored
            )

            // 4. Save to app-private directory
            val targetDir = File(context.filesDir, "checkin_photos").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "locket_stamp_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(targetDir, fileName)

            FileOutputStream(targetFile).use { outStream ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, outStream)
                outStream.flush()
            }

            val savedUri = Uri.fromFile(targetFile).toString()
            Log.d(TAG, "Processed and saved image to: $savedUri (Filter: ${colorMode.title}, Target: ${targetDimension}px)")
            Result.success(savedUri)
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError during image processing pipeline (target: $targetDimension)", oom)
            Result.failure(Exception("Hết bộ nhớ khi xử lý ảnh: ${oom.message}", oom))
        } catch (t: Throwable) {
            Log.e(TAG, "Error during image processing pipeline", t)
            Result.failure(if (t is Exception) t else Exception(t.message, t))
        } finally {
            // Clean up temporary bitmaps safely
            try {
                if (decodedBitmap != null && decodedBitmap != squareBitmap && !decodedBitmap.isRecycled) {
                    decodedBitmap.recycle()
                }
                if (squareBitmap != null && squareBitmap != processedBitmap && !squareBitmap.isRecycled) {
                    squareBitmap.recycle()
                }
                if (processedBitmap != null && !processedBitmap.isRecycled) {
                    processedBitmap.recycle()
                }
            } catch (ignored: Throwable) {
            }
        }
    }

    /**
     * Measure image dimensions first using inJustDecodeBounds, calculate optimal inSampleSize,
     * and only then allocate memory for the sampled bitmap.
     */
    private fun decodeSampledBitmap(context: Context, source: String, targetDimension: Int): Bitmap? {
        return try {
            when {
                source.startsWith("content://") -> {
                    val uri = Uri.parse(source)
                    var rotation = 0

                    // Step 1: Read EXIF orientation
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        try {
                            val exif = ExifInterface(stream)
                            rotation = exifToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
                        } catch (e: Throwable) {
                            Log.w(TAG, "Failed to read EXIF from content stream", e)
                        }
                    }

                    // Step 2: Measure bounds
                    val boundsOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, boundsOptions)
                    }

                    if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                        return null
                    }

                    // Step 3: Compute inSampleSize
                    val sampleSize = calculateInSampleSize(boundsOptions, targetDimension)

                    // Step 4: Decode sampled bitmap
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val sampled = context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, decodeOptions)
                    }

                    if (sampled != null && rotation != 0) {
                        val rotated = rotateBitmap(sampled, rotation.toFloat())
                        if (rotated != sampled && !sampled.isRecycled) {
                            sampled.recycle()
                        }
                        rotated
                    } else {
                        sampled
                    }
                }

                source.startsWith("file://") || source.startsWith("/") -> {
                    val filePath = if (source.startsWith("file://")) Uri.parse(source).path ?: source else source
                    var rotation = 0

                    // Step 1: Read EXIF orientation
                    try {
                        val exif = ExifInterface(filePath)
                        rotation = exifToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
                    } catch (e: Throwable) {
                        Log.w(TAG, "Failed to read EXIF from file: $filePath", e)
                    }

                    // Step 2: Measure bounds
                    val boundsOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(filePath, boundsOptions)

                    if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                        return null
                    }

                    // Step 3: Compute inSampleSize
                    val sampleSize = calculateInSampleSize(boundsOptions, targetDimension)

                    // Step 4: Decode sampled bitmap
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    val sampled = BitmapFactory.decodeFile(filePath, decodeOptions)

                    if (sampled != null && rotation != 0) {
                        val rotated = rotateBitmap(sampled, rotation.toFloat())
                        if (rotated != sampled && !sampled.isRecycled) {
                            sampled.recycle()
                        }
                        rotated
                    } else {
                        sampled
                    }
                }

                source.startsWith("http://") || source.startsWith("https://") -> {
                    val url = URL(source)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val bytes = connection.inputStream.use { it.readBytes() }

                    val boundsOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)

                    val sampleSize = calculateInSampleSize(boundsOptions, targetDimension)
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
                }

                else -> {
                    val file = File(source)
                    if (!file.exists()) return null

                    val boundsOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

                    val sampleSize = calculateInSampleSize(boundsOptions, targetDimension)
                    val decodeOptions = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                    BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                }
            }
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "OutOfMemoryError in decodeSampledBitmap", oom)
            null
        } catch (t: Throwable) {
            Log.e(TAG, "Error decoding bitmap from source: $source", t)
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, targetDimension: Int): Int {
        val rawWidth = options.outWidth
        val rawHeight = options.outHeight
        val minRawDim = min(rawWidth, rawHeight)

        var inSampleSize = 1
        // We want minRawDim / inSampleSize to be roughly targetDimension (keeping high crispness)
        while ((minRawDim / (inSampleSize * 2)) >= targetDimension) {
            inSampleSize *= 2
        }
        return max(1, inSampleSize)
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
            val cropped = Bitmap.createBitmap(bitmap, cropX, cropY, minDim, minDim)
            if (cropped != bitmap && !bitmap.isRecycled) {
                bitmap.recycle()
            }
            cropped
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
