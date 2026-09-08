package com.example.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Nen anh truoc khi tai len may chu.
 *
 * Anh chup bang dien thoai hien nay thuong 8-50 megapixel, mot tam co the 5-15 MB.
 * Tai thang len vua ton tien luu tru vua lam nguoi xem bang tin phai tai ve mot khoi
 * du lieu ma man hinh dien thoai khong the hien het duoc.
 *
 * Lop nay tao ra hai tep cho moi anh:
 *  - ban day du  (canh dai toi da FULL_MAX_PX) de xem o man hinh chi tiet
 *  - ban thu nho (canh dai toi da THUMB_MAX_PX) de hien o bang tin
 */
object ImageCompressor {

    private const val TAG = "ImageCompressor"

    /** Canh dai nhat cua ban day du, tinh bang diem anh. */
    const val FULL_MAX_PX = 1600

    /** Canh dai nhat cua ban thu nho. */
    const val THUMB_MAX_PX = 400

    private const val FULL_QUALITY = 85
    private const val THUMB_QUALITY = 75

    /** Thu muc tam giu anh da nen, cho toi khi tai len xong thi xoa. */
    private const val OUTPUT_DIR = "upload_staging"

    data class Result(
        val fullFile: File,
        val thumbFile: File,
        /** Ti le rong / cao cua anh goc, dung cho bo cuc masonry o bang tin. */
        val aspectRatio: Float
    )

    /**
     * Doc anh tu [source], nen lai va ghi ra hai tep trong thu muc tam.
     *
     * Tra ve `null` neu khong doc duoc anh hoac may het bo nho. Ben goi phai xu ly
     * truong hop null chu khong duoc coi la luon thanh cong.
     */
    fun compress(context: Context, source: Uri): Result? {
        return try {
            // Buoc 1: chi doc kich thuoc, chua nap diem anh nao vao bo nho.
            //
            // Luu y: voi inJustDecodeBounds = true thi decodeStream() LUON tra ve null va ghi
            // ket qua vao `bounds`. Vi vay khong duoc dat `?: return null` sau khoi use{} —
            // lam vay se coi moi anh doc duoc la anh hong. Phai kiem tra rieng luong doc.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val stream = context.contentResolver.openInputStream(source) ?: return null
            stream.use { BitmapFactory.decodeStream(it, null, bounds) }

            val srcW = bounds.outWidth
            val srcH = bounds.outHeight
            if (srcW <= 0 || srcH <= 0) return null

            val aspectRatio = srcW.toFloat() / srcH.toFloat()

            val outDir = File(context.filesDir, OUTPUT_DIR).apply { if (!exists()) mkdirs() }
            val stem = UUID.randomUUID().toString()

            val full = decodeScaled(context, source, srcW, srcH, FULL_MAX_PX)
                ?: return null
            val rotatedFull = applyExifRotation(context, source, full)
            val fullFile = File(outDir, "${stem}_full.jpg")
            writeJpeg(rotatedFull, fullFile, FULL_QUALITY)

            // Ban thu nho thu nho tu ban day du chu khong doc lai tep goc:
            // it viec hon va ket qua giong nhau.
            val thumb = scaleToLongestEdge(rotatedFull, THUMB_MAX_PX)
            val thumbFile = File(outDir, "${stem}_thumb.jpg")
            writeJpeg(thumb, thumbFile, THUMB_QUALITY)

            if (thumb !== rotatedFull) thumb.recycle()
            rotatedFull.recycle()
            if (full !== rotatedFull) full.recycle()

            Result(fullFile, thumbFile, aspectRatio)
        } catch (e: OutOfMemoryError) {
            // OutOfMemoryError ke thua Error chu khong phai Exception, nen catch (e: Exception)
            // o tren se KHONG bat duoc no. Anh do phan giai rat cao rat de roi vao day.
            Log.e(TAG, "Het bo nho khi nen anh", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Khong nen duoc anh", e)
            null
        }
    }

    /** Xoa cac tep tam da tai len xong. An toan khi tep khong con ton tai. */
    fun deleteStaged(vararg files: File?) {
        files.filterNotNull().forEach { f ->
            runCatching { if (f.exists()) f.delete() }
        }
    }

    /**
     * Nap anh o do phan giai vua du. `inSampleSize` chi nhan luy thua cua 2, nen buoc nay
     * chi dua anh ve *gan* kich thuoc mong muon; [scaleToLongestEdge] lo not phan con lai.
     */
    private fun decodeScaled(
        context: Context,
        source: Uri,
        srcW: Int,
        srcH: Int,
        maxEdge: Int
    ): Bitmap? {
        var sample = 1
        while (max(srcW, srcH) / sample > maxEdge * 2) sample *= 2

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val decoded = context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        val scaled = scaleToLongestEdge(decoded, maxEdge)
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }

    /** Thu nho sao cho canh dai nhat bang [maxEdge]. Anh nho san thi tra ve nguyen. */
    private fun scaleToLongestEdge(src: Bitmap, maxEdge: Int): Bitmap {
        val longest = max(src.width, src.height)
        if (longest <= maxEdge) return src
        val ratio = maxEdge.toFloat() / longest.toFloat()
        val w = (src.width * ratio).roundToInt().coerceAtLeast(1)
        val h = (src.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, w, h, true)
    }

    /**
     * May anh dien thoai thuong luu anh theo huong cam bien roi ghi huong that vao the EXIF.
     * Bo qua buoc nay thi anh chup doc se hien thi nam ngang.
     */
    private fun applyExifRotation(context: Context, source: Uri, bitmap: Bitmap): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(source)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            Log.w(TAG, "Khong doc duoc EXIF, giu nguyen huong anh", e)
            ExifInterface.ORIENTATION_NORMAL
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun writeJpeg(bitmap: Bitmap, target: File, quality: Int) {
        FileOutputStream(target).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
    }
}
