package com.example.data.upload

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.database.LuminaDatabase
import com.example.data.local.entity.PendingUploadEntity
import com.example.data.media.ImageCompressor
import com.example.data.model.Post
import com.example.data.remote.FirestorePostService
import com.example.data.remote.StorageService
import java.io.File

/**
 * Lay tung bai trong hang doi ra, tai anh len Cloud Storage roi ghi bai vao Firestore.
 *
 * Chay nen nen nguoi dung dong app hay mat song giua chung thi bai van khong mat.
 * WorkManager tu goi lai theo lich luy thoai khi worker tra ve [Result.retry].
 */
class PostUploadWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "PostUploadWorker"

        /** Qua so lan nay thi coi nhu bai hong, bo ra khoi hang doi de khong thu mai. */
        private const val MAX_ATTEMPTS = 6
    }

    private val dao by lazy { LuminaDatabase.getInstance(applicationContext).pendingUploadDao() }
    private val storageService by lazy { StorageService() }
    private val postService by lazy { FirestorePostService() }

    override suspend fun doWork(): Result {
        val queue = dao.getAll()
        if (queue.isEmpty()) return Result.success()

        var needsRetry = false

        for (item in queue) {
            when (uploadOne(item)) {
                Outcome.DONE -> Unit
                Outcome.RETRY -> needsRetry = true
                Outcome.GIVE_UP -> Unit
            }
        }

        return if (needsRetry) Result.retry() else Result.success()
    }

    private enum class Outcome { DONE, RETRY, GIVE_UP }

    private suspend fun uploadOne(item: PendingUploadEntity): Outcome {
        val fullFiles = PendingUploadEntity.splitPaths(item.fullPaths).map(::File)
        val thumbFiles = PendingUploadEntity.splitPaths(item.thumbPaths).map(::File)

        // Tep tam bi he thong don dep hoac nguoi dung xoa du lieu app: khong con gi de tai len.
        if (fullFiles.any { !it.exists() }) {
            Log.w(TAG, "Bai ${item.postId} mat tep anh tam, bo khoi hang doi")
            dao.remove(item.postId)
            return Outcome.GIVE_UP
        }

        return try {
            val (fullUrls, thumbUrls) = storageService.uploadPostMedia(
                userId = item.userId,
                postId = item.postId,
                fullFiles = fullFiles,
                thumbFiles = thumbFiles
            )

            postService.createPost(
                post = buildPost(item, fullUrls, thumbUrls),
                userId = item.userId,
                mediaThumbUrls = thumbUrls
            )

            dao.remove(item.postId)
            ImageCompressor.deleteStaged(*(fullFiles + thumbFiles).toTypedArray())
            Log.d(TAG, "Da dang xong bai ${item.postId}")
            Outcome.DONE
        } catch (e: Exception) {
            val message = storageService.describeError(e)
            dao.markAttemptFailed(item.postId, message)
            Log.w(TAG, "Tai len that bai cho bai ${item.postId}: $message", e)

            if (item.attemptCount + 1 >= MAX_ATTEMPTS) {
                // Van giu ban ghi lai chu khong xoa: nguoi dung con thay bai dang ket
                // va biet vi sao, thay vi bai bien mat khong dau vet.
                Log.e(TAG, "Bai ${item.postId} that bai ${MAX_ATTEMPTS} lan, ngung thu lai")
                Outcome.GIVE_UP
            } else {
                Outcome.RETRY
            }
        }
    }

    private fun buildPost(
        item: PendingUploadEntity,
        fullUrls: List<String>,
        thumbUrls: List<String>
    ): Post = Post(
        id = item.postId,
        userId = item.userId,
        authorName = item.authorName,
        authorHandle = item.authorHandle,
        authorAvatarUrl = item.authorAvatarUrl,
        hasRecentCheckIn = true,
        locationLabel = item.locationLabel,
        provinceCode = item.provinceCode,
        timeAgo = "Vừa xong",
        caption = item.caption,
        // Anh mau co san tren mang dung truoc, roi den anh nguoi dung vua tai len —
        // giu dung thu tu ma nguoi dung nhin thay o man hinh soan bai.
        mediaUrls = PendingUploadEntity.splitPaths(item.remoteUrls) + fullUrls,
        aspectRatio = item.aspectRatio,
        attachedTrip = null,
        tags = listOf("Levart", "VietnamTravel", "GenZ")
    )
}
