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
        LuminaFrameEntity::class,
        AppSettingsEntity::class,
        PendingUploadEntity::class
    ],
    // MOI lan doi luoc do (them cot, doi kieu) deu phai tang so nay. Giu nguyen so cu
    // ma doi luoc do thi fallbackToDestructiveMigration() KHONG cuu duoc: Room so hash
    // luoc do, thay lech la nem IllegalStateException "cannot verify the data integrity".
    version = 11,
    exportSchema = false
)
@TypeConverters(LuminaTypeConverters::class)
abstract class LuminaDatabase : RoomDatabase() {

    abstract fun provinceDao(): ProvinceDao
    abstract fun placeDao(): PlaceDao
    abstract fun frameDao(): LuminaFrameDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun pendingUploadDao(): PendingUploadDao

    companion object {
        // KHONG doi ten tep nay: Room se tao co so du lieu moi rong va toan bo
        // du lieu danh muc trong may cua nguoi dung bi mat.
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
