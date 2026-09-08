package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Hang doi bai viet cho tai len may chu.
 *
 * Nguoi dung bam Dang la bai vao day ngay lap tuc, ke ca khi khong co mang.
 * WorkManager se lay tung ban ghi ra, tai anh len Cloud Storage roi ghi bai vao Firestore.
 * Nho vay mat song giua chung khong lam mat bai viet da soan.
 *
 * Anh da nen nam trong `filesDir/upload_staging`, chi luu duong dan tep o day.
 */
@Entity(tableName = "pending_uploads")
data class PendingUploadEntity(
    /** Trung voi id cua bai viet se tao tren Firestore, de khong bi dang trung hai lan. */
    @PrimaryKey
    @ColumnInfo(name = "post_id")
    val postId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    // Ba truong tac gia phai luu kem: FirestorePostService doc chung tu chinh document
    // bai viet (dong 43-45) chu khong tra cuu lai ho so, nen de trong se ra bai khong ten.
    @ColumnInfo(name = "author_name")
    val authorName: String,

    @ColumnInfo(name = "author_handle")
    val authorHandle: String,

    @ColumnInfo(name = "author_avatar_url")
    val authorAvatarUrl: String,

    @ColumnInfo(name = "caption")
    val caption: String,

    @ColumnInfo(name = "location_label")
    val locationLabel: String,

    @ColumnInfo(name = "province_code")
    val provinceCode: String,

    /** Duong dan tuyet doi cac tep anh ban day du, ngan cach bang dau xuong dong. */
    @ColumnInfo(name = "full_paths")
    val fullPaths: String,

    /** Duong dan tuyet doi cac tep anh ban thu nho, cung thu tu voi [fullPaths]. */
    @ColumnInfo(name = "thumb_paths")
    val thumbPaths: String,

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: Float,

    /**
     * Anh da san tren mang (anh mau nguoi dung chon them). Khong can tai len,
     * chi ghep vao dau danh sach khi tao bai. Ngan cach bang dau xuong dong.
     */
    @ColumnInfo(name = "remote_urls")
    val remoteUrls: String = "",

    /** Chuyen di dinh kem, luu duoi dang JSON. Rong nghia la khong dinh kem gi. */
    @ColumnInfo(name = "attached_trip_json")
    val attachedTripJson: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** So lan da thu tai len that bai. Dung de bao cho nguoi dung biet bai dang ket. */
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int = 0,

    /** Loi cua lan thu gan nhat, de hien trong giao dien thay vi im lang. */
    @ColumnInfo(name = "last_error")
    val lastError: String = ""
) {
    companion object {
        private const val SEPARATOR = "\n"

        fun joinPaths(paths: List<String>): String = paths.joinToString(SEPARATOR)

        fun splitPaths(joined: String): List<String> =
            if (joined.isBlank()) emptyList() else joined.split(SEPARATOR)
    }
}
