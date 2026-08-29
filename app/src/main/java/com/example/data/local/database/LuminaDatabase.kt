package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.converters.LuminaTypeConverters
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.local.seed.DatabaseSeeder

@Database(
    entities = [
        ProvinceEntity::class,
        PlaceEntity::class,
        CheckInEntity::class,
        TripEntity::class,
        PostEntity::class,
        CommentEntity::class,
        LuminaFrameEntity::class,
        AchievementBadgeEntity::class,
        NotificationEntity::class,
        SavedBoardEntity::class,
        AppSettingsEntity::class,
        UserFollowEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(LuminaTypeConverters::class)
abstract class LuminaDatabase : RoomDatabase() {

    abstract fun provinceDao(): ProvinceDao
    abstract fun placeDao(): PlaceDao
    abstract fun checkInDao(): CheckInDao
    abstract fun tripDao(): TripDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun frameDao(): LuminaFrameDao
    abstract fun badgeDao(): AchievementBadgeDao
    abstract fun notificationDao(): NotificationDao
    abstract fun savedBoardDao(): SavedBoardDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun userFollowDao(): UserFollowDao

    companion object {
        private const val DATABASE_NAME = "lumina_vietnam_travel.db"

        @Volatile
        private var INSTANCE: LuminaDatabase? = null

        fun getInstance(context: Context): LuminaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): LuminaDatabase {
            return Room.databaseBuilder(
                context,
                LuminaDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Trigger async database seeding when created
                        INSTANCE?.let { database ->
                            DatabaseSeeder.seedDatabase(database)
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Ensure seeds exist if table is empty
                        INSTANCE?.let { database ->
                            DatabaseSeeder.seedDatabase(database)
                        }
                    }
                })
                .build()
        }
    }
}
