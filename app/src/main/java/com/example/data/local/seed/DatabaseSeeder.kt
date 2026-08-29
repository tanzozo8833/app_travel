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

                if (currentProvinceCount == 0) {
                    Log.d(TAG, "Starting database seeding (provinces, places, frames)...")
                    database.provinceDao().insertAll(ProvinceSeed.data)
                    database.placeDao().insertAll(PlaceSeed.data)
                    database.frameDao().insertAll(FrameSeed.data)
                    Log.d(TAG, "Database seeding completed with 34 provinces.")
                } else {
                    if (currentProvinceCount < ProvinceSeed.data.size) {
                        Log.d(TAG, "Database has $currentProvinceCount provinces. Updating to ${ProvinceSeed.data.size} provinces...")
                        database.provinceDao().insertAll(ProvinceSeed.data)
                        database.placeDao().insertAll(PlaceSeed.data)
                        database.frameDao().insertAll(FrameSeed.data)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed database", e)
            }
        }
    }
}
