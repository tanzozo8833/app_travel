package com.example.data.remote

import android.util.Log
import com.example.data.model.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestorePersonalDataService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    companion object {
        private const val TAG = "FirestorePersonalData"
        private const val USERS = "users"
        private const val CHECK_INS = "check_ins"
        private const val TRIPS = "trips"
        private const val SAVED_BOARDS = "saved_boards"
        private const val NOTIFICATIONS = "notifications"
    }

    private fun userDoc(userId: String) = firestore.collection(USERS).document(userId)

    // ==========================================
    // 1. CHECK-INS (CON DẤU HỘ CHIẾU)
    // ==========================================

    private fun documentToCheckInRecord(doc: DocumentSnapshot, defaultUserId: String): CheckInRecord? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val userId = doc.getString("userId") ?: defaultUserId
            val placeName = doc.getString("placeName") ?: ""
            val provinceName = doc.getString("provinceName") ?: ""
            val provinceCode = doc.getString("provinceCode") ?: ""
            val timestamp = doc.getString("timestamp") ?: ""
            val formattedDate = doc.getString("formattedDate") ?: ""
            val methodStr = doc.getString("method") ?: "QUICK"
            val method = if (methodStr == "PHOTO") CheckInMethod.PHOTO else CheckInMethod.QUICK
            val photoUrl = doc.getString("photoUrl")
            val framedPhotoUrl = doc.getString("framedPhotoUrl")
            val frameId = doc.getString("frameId")
            val caption = doc.getString("caption")
            val trustScore = doc.getDouble("trustScore")?.toFloat() ?: 1.0f
            val lat = doc.getDouble("lat") ?: 0.0
            val lng = doc.getDouble("lng") ?: 0.0

            CheckInRecord(
                id = id,
                userId = userId,
                placeName = placeName,
                provinceName = provinceName,
                provinceCode = provinceCode,
                timestamp = timestamp,
                formattedDate = formattedDate,
                method = method,
                photoUrl = photoUrl,
                framedPhotoUrl = framedPhotoUrl,
                frameId = frameId,
                caption = caption,
                trustScore = trustScore,
                lat = lat,
                lng = lng
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping CheckInRecord ${doc.id}", e)
            null
        }
    }

    fun observeCheckIns(userId: String): Flow<List<CheckInRecord>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = userDoc(userId).collection(CHECK_INS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for check-ins of $userId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        documentToCheckInRecord(doc, userId)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveCheckIn(userId: String, record: CheckInRecord) {
        if (userId.isBlank() || record.id.isBlank()) return
        try {
            val data = hashMapOf(
                "id" to record.id,
                "userId" to userId,
                "placeName" to record.placeName,
                "provinceName" to record.provinceName,
                "provinceCode" to record.provinceCode,
                "timestamp" to record.timestamp,
                "formattedDate" to record.formattedDate,
                "method" to record.method.name,
                "photoUrl" to record.photoUrl,
                "framedPhotoUrl" to record.framedPhotoUrl,
                "frameId" to record.frameId,
                "caption" to record.caption,
                "trustScore" to record.trustScore,
                "lat" to record.lat,
                "lng" to record.lng,
                "createdAt" to System.currentTimeMillis()
            )
            userDoc(userId).collection(CHECK_INS).document(record.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved check-in ${record.id} to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving check-in for $userId", e)
        }
    }

    // ==========================================
    // 2. TRIPS (LỊCH TRÌNH CHUYẾN ĐI)
    // ==========================================

    @Suppress("UNCHECKED_CAST")
    private fun documentToTrip(doc: DocumentSnapshot, defaultUserId: String): Trip? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val userId = doc.getString("userId") ?: defaultUserId
            val title = doc.getString("title") ?: "Chuyến đi"
            val destination = doc.getString("destination") ?: ""
            val provinceCode = doc.getString("provinceCode") ?: ""
            val coverUrl = doc.getString("coverUrl") ?: ""
            val startDate = doc.getString("startDate") ?: ""
            val endDate = doc.getString("endDate") ?: ""
            val durationText = doc.getString("durationText") ?: ""
            val statusStr = doc.getString("status") ?: "UPCOMING"
            val status = try { TripStatus.valueOf(statusStr) } catch (_: Exception) { TripStatus.UPCOMING }
            val totalCost = doc.getLong("totalEstimatedCostVnd") ?: 2400000L
            val spotsCount = doc.getLong("spotsCount")?.toInt() ?: 0
            val groupType = doc.getString("groupType") ?: "Cặp đôi"
            val styleTags = (doc.get("styleTags") as? List<String>) ?: emptyList()
            val isAiGenerated = doc.getBoolean("isAiGenerated") ?: true

            val weatherMap = doc.get("weatherSummary") as? Map<String, Any>
            val weatherSummary = if (weatherMap != null) {
                WeatherSummary(
                    temperature = weatherMap["temperature"] as? String ?: "22°C",
                    weatherCondition = weatherMap["weatherCondition"] as? String ?: "Nắng đẹp",
                    aiTip = weatherMap["aiTip"] as? String ?: ""
                )
            } else {
                WeatherSummary()
            }

            val rawDays = doc.get("days") as? List<Map<String, Any>> ?: emptyList()
            val days = rawDays.map { dayMap ->
                val dayNumber = (dayMap["dayNumber"] as? Number)?.toInt() ?: 1
                val dateText = dayMap["dateText"] as? String ?: ""
                val dayTitle = dayMap["title"] as? String ?: ""
                val rawItems = dayMap["items"] as? List<Map<String, Any>> ?: emptyList()
                val items = rawItems.map { itemMap ->
                    val catStr = itemMap["category"] as? String ?: "PHOTO"
                    val cat = try { PlaceCategory.valueOf(catStr) } catch (_: Exception) { PlaceCategory.PHOTO }
                    TripItem(
                        id = itemMap["id"] as? String ?: "",
                        timeSlot = itemMap["timeSlot"] as? String ?: "",
                        title = itemMap["title"] as? String ?: "",
                        description = itemMap["description"] as? String ?: "",
                        locationName = itemMap["locationName"] as? String ?: "",
                        category = cat,
                        costVnd = (itemMap["costVnd"] as? Number)?.toLong() ?: 0L,
                        bookingLabel = itemMap["bookingLabel"] as? String,
                        bookingUrl = itemMap["bookingUrl"] as? String,
                        isCheckedIn = itemMap["isCheckedIn"] as? Boolean ?: false
                    )
                }
                TripDay(dayNumber, dateText, dayTitle, items)
            }

            Trip(
                id = id,
                userId = userId,
                title = title,
                destination = destination,
                provinceCode = provinceCode,
                coverUrl = coverUrl,
                startDate = startDate,
                endDate = endDate,
                durationText = durationText,
                status = status,
                totalEstimatedCostVnd = totalCost,
                spotsCount = spotsCount,
                groupType = groupType,
                styleTags = styleTags,
                isAiGenerated = isAiGenerated,
                weatherSummary = weatherSummary,
                days = days
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping Trip ${doc.id}", e)
            null
        }
    }

    fun observeTrips(userId: String): Flow<List<Trip>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = userDoc(userId).collection(TRIPS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for trips of $userId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        documentToTrip(doc, userId)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveTrip(userId: String, trip: Trip) {
        if (userId.isBlank() || trip.id.isBlank()) return
        try {
            val daysList = trip.days.map { day ->
                mapOf(
                    "dayNumber" to day.dayNumber,
                    "dateText" to day.dateText,
                    "title" to day.title,
                    "items" to day.items.map { item ->
                        mapOf(
                            "id" to item.id,
                            "timeSlot" to item.timeSlot,
                            "title" to item.title,
                            "description" to item.description,
                            "locationName" to item.locationName,
                            "category" to item.category.name,
                            "costVnd" to item.costVnd,
                            "bookingLabel" to item.bookingLabel,
                            "bookingUrl" to item.bookingUrl,
                            "isCheckedIn" to item.isCheckedIn
                        )
                    }
                )
            }

            val weatherMap = mapOf(
                "temperature" to trip.weatherSummary.temperature,
                "weatherCondition" to trip.weatherSummary.weatherCondition,
                "aiTip" to trip.weatherSummary.aiTip
            )

            val data = hashMapOf(
                "id" to trip.id,
                "userId" to userId,
                "title" to trip.title,
                "destination" to trip.destination,
                "provinceCode" to trip.provinceCode,
                "coverUrl" to trip.coverUrl,
                "startDate" to trip.startDate,
                "endDate" to trip.endDate,
                "durationText" to trip.durationText,
                "status" to trip.status.name,
                "totalEstimatedCostVnd" to trip.totalEstimatedCostVnd,
                "spotsCount" to trip.spotsCount,
                "groupType" to trip.groupType,
                "styleTags" to trip.styleTags,
                "isAiGenerated" to trip.isAiGenerated,
                "weatherSummary" to weatherMap,
                "days" to daysList,
                "createdAt" to System.currentTimeMillis()
            )

            userDoc(userId).collection(TRIPS).document(trip.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved trip ${trip.id} to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving trip for $userId", e)
        }
    }

    suspend fun getTripById(userId: String, tripId: String): Trip? {
        if (userId.isBlank() || tripId.isBlank()) return null
        return try {
            val doc = userDoc(userId).collection(TRIPS).document(tripId).get().await()
            documentToTrip(doc, userId)
        } catch (e: Exception) {
            Log.w(TAG, "Error getting trip $tripId for $userId", e)
            null
        }
    }

    suspend fun deleteTrip(userId: String, tripId: String) {
        if (userId.isBlank() || tripId.isBlank()) return
        try {
            userDoc(userId).collection(TRIPS).document(tripId).delete().await()
            Log.d(TAG, "Deleted trip $tripId for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting trip $tripId for $userId", e)
        }
    }

    // ==========================================
    // 3. SAVED BOARDS (BỘ SƯU TẬP ĐÃ LƯU)
    // ==========================================

    @Suppress("UNCHECKED_CAST")
    private fun documentToSavedBoard(doc: DocumentSnapshot, defaultUserId: String): SavedBoard? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val name = doc.getString("name") ?: doc.getString("title") ?: "Bộ sưu tập"
            val coverUrl = doc.getString("coverUrl") ?: ""
            val postCount = (doc.getLong("postCount") ?: doc.getLong("itemCount"))?.toInt() ?: 0
            val isPrivate = doc.getBoolean("isPrivate") ?: true

            SavedBoard(
                id = id,
                name = name,
                coverUrl = coverUrl,
                postCount = postCount,
                isPrivate = isPrivate
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping SavedBoard ${doc.id}", e)
            null
        }
    }

    fun observeSavedBoards(userId: String): Flow<List<SavedBoard>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = userDoc(userId).collection(SAVED_BOARDS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for saved boards of $userId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        documentToSavedBoard(doc, userId)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveSavedBoard(userId: String, board: SavedBoard) {
        if (userId.isBlank() || board.id.isBlank()) return
        try {
            val data = hashMapOf(
                "id" to board.id,
                "name" to board.name,
                "coverUrl" to board.coverUrl,
                "postCount" to board.postCount,
                "isPrivate" to board.isPrivate,
                "userId" to userId,
                "updatedAt" to System.currentTimeMillis()
            )
            userDoc(userId).collection(SAVED_BOARDS).document(board.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d(TAG, "Saved board ${board.id} to Firestore for user $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving board for $userId", e)
        }
    }

    suspend fun deleteSavedBoard(userId: String, boardId: String) {
        if (userId.isBlank() || boardId.isBlank()) return
        try {
            userDoc(userId).collection(SAVED_BOARDS).document(boardId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting board $boardId for $userId", e)
        }
    }

    // ==========================================
    // 4. NOTIFICATIONS (THÔNG BÁO)
    // ==========================================

    private fun documentToNotification(doc: DocumentSnapshot): NotificationItem? {
        if (!doc.exists()) return null
        return try {
            val id = doc.getString("id") ?: doc.id
            val isSocial = doc.getBoolean("isSocial") ?: true
            val actorName = doc.getString("actorName")
            val actorAvatarUrl = doc.getString("actorAvatarUrl")
            val title = doc.getString("title") ?: "Thông báo"
            val message = doc.getString("message") ?: ""
            val timeAgo = doc.getString("timeAgo") ?: "Vừa xong"
            val previewUrl = doc.getString("previewUrl")
            val isRead = doc.getBoolean("isRead") ?: false
            val actionType = doc.getString("actionType")

            NotificationItem(
                id = id,
                isSocial = isSocial,
                actorName = actorName,
                actorAvatarUrl = actorAvatarUrl,
                title = title,
                message = message,
                timeAgo = timeAgo,
                previewUrl = previewUrl,
                isRead = isRead,
                actionType = actionType
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping Notification ${doc.id}", e)
            null
        }
    }

    fun observeNotifications(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = userDoc(userId).collection(NOTIFICATIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for notifications of $userId", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        documentToNotification(doc)
                    }
                    trySend(list)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    suspend fun saveNotification(userId: String, notif: NotificationItem) {
        if (userId.isBlank() || notif.id.isBlank()) return
        try {
            val data = hashMapOf(
                "id" to notif.id,
                "isSocial" to notif.isSocial,
                "actorName" to notif.actorName,
                "actorAvatarUrl" to notif.actorAvatarUrl,
                "title" to notif.title,
                "message" to notif.message,
                "timeAgo" to notif.timeAgo,
                "previewUrl" to notif.previewUrl,
                "isRead" to notif.isRead,
                "actionType" to notif.actionType,
                "createdAt" to System.currentTimeMillis()
            )
            userDoc(userId).collection(NOTIFICATIONS).document(notif.id)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification for $userId", e)
        }
    }

    suspend fun markNotificationAsRead(userId: String, notifId: String) {
        if (userId.isBlank() || notifId.isBlank()) return
        try {
            userDoc(userId).collection(NOTIFICATIONS).document(notifId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notif as read for $userId", e)
        }
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        if (userId.isBlank()) return
        try {
            val unread = userDoc(userId).collection(NOTIFICATIONS).whereEqualTo("isRead", false).get().await()
            for (doc in unread.documents) {
                doc.reference.update("isRead", true).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all notifs as read for $userId", e)
        }
    }

    // ==========================================
    // ACCOUNT DELETION CLEANUP (TẤT CẢ DỮ LIỆU CÁ NHÂN)
    // ==========================================

    suspend fun deleteAllPersonalData(userId: String) {
        if (userId.isBlank()) return
        val collections = listOf(CHECK_INS, TRIPS, SAVED_BOARDS, NOTIFICATIONS)
        for (colName in collections) {
            try {
                val snapshot = userDoc(userId).collection(colName).get().await()
                for (doc in snapshot.documents) {
                    doc.reference.delete().await()
                }
                Log.d(TAG, "Deleted all $colName for user $userId")
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting subcollection $colName for $userId", e)
            }
        }
    }
}
