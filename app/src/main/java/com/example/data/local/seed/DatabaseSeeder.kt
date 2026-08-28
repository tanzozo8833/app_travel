package com.example.data.local.seed

import android.util.Log
import com.example.data.local.database.LuminaDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseSeeder {
    private const val TAG = "DatabaseSeeder"

    fun seedDatabase(database: LuminaDatabase, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        scope.launch {
            try {
                val currentProvinceCount = database.provinceDao().getCount()
                val currentUserProfileCount = database.userProfileDao().getCount()

                if (currentProvinceCount == 0) {
                    Log.d(TAG, "Starting full database seeding...")
                    database.provinceDao().insertAll(ProvinceSeed.data)
                    database.placeDao().insertAll(PlaceSeed.data)
                    database.userProfileDao().insertAll(UserProfileSeed.data)
                    database.tripDao().insertAll(TripSeed.data)
                    database.postDao().insertAll(PostSeed.data)
                    database.commentDao().insertAll(CommentSeed.data)
                    database.checkInDao().insertAll(CheckInSeed.data)
                    database.badgeDao().insertAll(BadgeSeed.data)
                    database.frameDao().insertAll(FrameSeed.data)
                    database.notificationDao().insertAll(NotificationSeed.data)
                    database.savedBoardDao().insertAll(SavedBoardSeed.data)
                    Log.d(TAG, "Full database seeding completed with 34 provinces and ${UserProfileSeed.data.size} profiles.")
                } else {
                    if (currentProvinceCount < ProvinceSeed.data.size) {
                        Log.d(TAG, "Database has $currentProvinceCount provinces. Updating to ${ProvinceSeed.data.size} provinces...")
                        database.provinceDao().insertAll(ProvinceSeed.data)
                        database.placeDao().insertAll(PlaceSeed.data)
                        database.frameDao().insertAll(FrameSeed.data)
                        database.badgeDao().insertAll(BadgeSeed.data)
                    }
                    if (currentUserProfileCount < UserProfileSeed.data.size) {
                        Log.d(TAG, "Database has $currentUserProfileCount profiles. Inserting missing seed profiles...")
                        database.userProfileDao().insertAll(UserProfileSeed.data)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed database", e)
            }
        }
    }
}
