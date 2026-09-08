package com.example.data.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.local.database.LuminaDatabase
import com.example.data.local.entity.PendingUploadEntity
import com.example.data.media.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cua ngo duy nhat de dua mot bai viet vao hang doi tai len.
 *
 * Nguoi dung bam Dang → anh duoc nen ngay tren luong nen → ghi vao bang `pending_uploads`
 * → dat lich cho [PostUploadWorker]. Toan bo phan con lai chay nen.
 */
object UploadQueue {

    private const val TAG = "UploadQueue"
    private const val WORK_NAME = "levart_post_upload"

    /** Ket qua cua viec xep hang, de man hinh soan bai biet noi gi voi nguoi dung. */
    sealed interface EnqueueResult {
        /** Da xep hang thanh cong. [pendingCount] la so bai dang cho tai len. */
        data class Queued(val postId: String, val pendingCount: Int) : EnqueueResult

        /** Khong doc duoc anh nao trong so anh nguoi dung chon. */
        data object NoUsableImage : EnqueueResult
    }

    suspend fun enqueuePost(
        context: Context,
        userId: String,
        authorName: String,
        authorHandle: String,
        authorAvatarUrl: String,
        caption: String,
        locationLabel: String,
        provinceCode: String,
        imageUris: List<Uri>,
        /** Anh da co san tren mang (anh mau). Giu nguyen, khong tai len lai. */
        remoteImageUrls: List<String> = emptyList()
    ): EnqueueResult = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext

        val fullPaths = mutableListOf<String>()
        val thumbPaths = mutableListOf<String>()
        var aspectRatio = 0.8f

        imageUris.forEachIndexed { index, uri ->
            val result = ImageCompressor.compress(appContext, uri)
            if (result == null) {
                // Mot anh hong khong nen lam hong ca bai: bo qua anh do, giu cac anh con lai.
                Log.w(TAG, "Bo qua anh khong doc duoc: $uri")
                return@forEachIndexed
            }
            fullPaths += result.fullFile.absolutePath
            thumbPaths += result.thumbFile.absolutePath
            // Ti le khung hinh lay theo anh dau tien, dung cho bo cuc masonry o bang tin.
            if (index == 0) aspectRatio = result.aspectRatio
        }

        if (fullPaths.isEmpty() && remoteImageUrls.isEmpty()) {
            return@withContext EnqueueResult.NoUsableImage
        }

        val dao = LuminaDatabase.getInstance(appContext).pendingUploadDao()
        val postId = "post_${UUID.randomUUID()}"

        dao.enqueue(
            PendingUploadEntity(
                postId = postId,
                userId = userId,
                authorName = authorName,
                authorHandle = authorHandle,
                authorAvatarUrl = authorAvatarUrl,
                caption = caption,
                locationLabel = locationLabel,
                provinceCode = provinceCode,
                fullPaths = PendingUploadEntity.joinPaths(fullPaths),
                thumbPaths = PendingUploadEntity.joinPaths(thumbPaths),
                aspectRatio = aspectRatio,
                remoteUrls = PendingUploadEntity.joinPaths(remoteImageUrls)
            )
        )

        schedule(appContext)
        EnqueueResult.Queued(postId, dao.getAll().size)
    }

    /**
     * Dat lich chay worker.
     *
     * [ExistingWorkPolicy.KEEP] chu khong phai REPLACE: neu dang co mot luot tai len chay do,
     * xep them bai moi khong duoc huy luot dang chay. Worker doc ca hang doi moi lan chay
     * nen bai moi van duoc xu ly.
     */
    fun schedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<PostUploadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    /** So bai dang cho tai len, de man hinh hien bang thong bao. */
    fun observePendingCount(context: Context) =
        LuminaDatabase.getInstance(context.applicationContext).pendingUploadDao().observeCount()

    fun observePending(context: Context) =
        LuminaDatabase.getInstance(context.applicationContext).pendingUploadDao().observeAll()

    /**
     * Xoa mot bai khoi hang doi theo yeu cau cua nguoi dung, kem cac tep anh tam.
     */
    suspend fun cancel(context: Context, postId: String) = withContext(Dispatchers.IO) {
        val dao = LuminaDatabase.getInstance(context.applicationContext).pendingUploadDao()
        dao.getById(postId)?.let { item ->
            val files = (PendingUploadEntity.splitPaths(item.fullPaths) +
                PendingUploadEntity.splitPaths(item.thumbPaths)).map(::File)
            ImageCompressor.deleteStaged(*files.toTypedArray())
        }
        dao.remove(postId)
    }
}
