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
                // Check if already seeded
                if (database.provinceDao().getCount() > 0) {
                    Log.d(TAG, "Database already contains data, skipping seed.")
                    return@launch
                }

                Log.d(TAG, "Starting comprehensive database seeding...")

                // 1. Seed Provinces
                database.provinceDao().insertAll(ProvinceSeed.data)

                // 2. Seed Places
                database.placeDao().insertAll(PlaceSeed.data)

                // 3. Seed User Profiles
                database.userProfileDao().insertAll(UserProfileSeed.data)

                // 4. Seed Trips
                database.tripDao().insertAll(TripSeed.data)

                // 5. Seed Posts
                database.postDao().insertAll(PostSeed.data)

                // 6. Seed Comments
                database.commentDao().insertAll(CommentSeed.data)

                // 7. Seed Check-ins
                database.checkInDao().insertAll(CheckInSeed.data)

                // 8. Seed Badges
                database.badgeDao().insertAll(BadgeSeed.data)

                // 9. Seed Frames
                database.frameDao().insertAll(FrameSeed.data)

                // 10. Seed Notifications
                database.notificationDao().insertAll(NotificationSeed.data)

                // 11. Seed Saved Boards
                database.savedBoardDao().insertAll(SavedBoardSeed.data)

                Log.d(TAG, "Database seeding completed successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed database", e)
            }
        }
    }
}
