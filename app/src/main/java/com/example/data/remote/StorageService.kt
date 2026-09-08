package com.example.data.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Tai anh len Cloud Storage for Firebase.
 *
 * Duong dan tren may chu duoc dat theo chu so huu:
 *
 *     posts/{uid}/{postId}/{n}.jpg        anh ban day du
 *     posts/{uid}/{postId}/{n}_thumb.jpg  anh ban thu nho
 *     checkins/{uid}/{checkInId}.jpg      anh dong dau
 *
 * Cau truc nay khop voi luat bao mat trong `storage.rules`: chi chu so huu ghi duoc
 * vao thu muc mang uid cua minh.
 */
class StorageService(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    companion object {
        private const val TAG = "StorageService"

        /**
         * Ma loi Firebase tra ve khi du an chua bat goi Blaze hoac chua tao bucket.
         * Tach rieng de bao loi cho dung thay vi noi chung chung la "khong co mang".
         */
        const val ERROR_NO_BUCKET = "STORAGE_KHONG_KHA_DUNG"
    }

    /**
     * Tai mot tep len va tra ve dia chi tai xuong cong khai.
     * Nem [StorageException] neu that bai, de ben goi quyet dinh co thu lai hay khong.
     */
    suspend fun uploadFile(localFile: File, remotePath: String): String {
        val ref = storage.reference.child(remotePath)
        ref.putFile(Uri.fromFile(localFile)).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Tai toan bo anh cua mot bai viet.
     *
     * @return cap (danh sach dia chi ban day du, danh sach dia chi ban thu nho), cung thu tu.
     */
    suspend fun uploadPostMedia(
        userId: String,
        postId: String,
        fullFiles: List<File>,
        thumbFiles: List<File>
    ): Pair<List<String>, List<String>> {
        val fullUrls = mutableListOf<String>()
        val thumbUrls = mutableListOf<String>()

        fullFiles.forEachIndexed { index, file ->
            fullUrls += uploadFile(file, "posts/$userId/$postId/$index.jpg")
        }
        thumbFiles.forEachIndexed { index, file ->
            thumbUrls += uploadFile(file, "posts/$userId/$postId/${index}_thumb.jpg")
        }

        return fullUrls to thumbUrls
    }

    /** Tai anh dong dau cua mot lan check-in. */
    suspend fun uploadCheckInPhoto(userId: String, checkInId: String, file: File): String =
        uploadFile(file, "checkins/$userId/$checkInId.jpg")

    /** Xoa toan bo anh cua mot bai viet khi nguoi dung xoa bai. */
    suspend fun deletePostMedia(userId: String, postId: String) {
        try {
            val folder = storage.reference.child("posts/$userId/$postId")
            folder.listAll().await().items.forEach { item ->
                runCatching { item.delete().await() }
                    .onFailure { Log.w(TAG, "Khong xoa duoc ${item.path}", it) }
            }
        } catch (e: Exception) {
            // Xoa anh that bai khong nen chan viec xoa bai viet.
            Log.w(TAG, "Khong xoa duoc thu muc anh cua bai $postId", e)
        }
    }

    /**
     * Doi ma loi cua Firebase thanh cau tieng Viet hien duoc cho nguoi dung.
     * Rieng loi thieu bucket duoc noi ro vi do la loi cau hinh du an, khong phai loi mang.
     */
    fun describeError(e: Throwable): String {
        val http = (e as? StorageException)?.httpResultCode
        return when {
            // 404 = du an chua tao bucket Storage nao. 402/403 = co bucket nhung du an
            // dang o goi Spark nen bi tu choi. Ca hai deu la loi cau hinh du an chu khong
            // phai loi mang, nen phai noi ro thay vi bao "khong co mang".
            http == 404 ->
                "$ERROR_NO_BUCKET: du an Firebase chua tao bucket Cloud Storage."
            http == 402 || http == 403 ->
                "$ERROR_NO_BUCKET: du an Firebase chua bat Cloud Storage (can goi Blaze)."
            e is StorageException && e.errorCode == StorageException.ERROR_RETRY_LIMIT_EXCEEDED ->
                "Mang qua yeu, chua tai anh len duoc."
            else -> e.localizedMessage ?: e.javaClass.simpleName
        }
    }
}
