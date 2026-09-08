package com.example

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.local.database.LuminaDatabase
import com.example.data.local.seed.DatabaseSeeder
import com.example.data.repository.LuminaRepository

class LuminaApplication : Application(), ImageLoaderFactory {
    private var customImageLoader: ImageLoader? = null

    val database: LuminaDatabase by lazy {
        LuminaDatabase.getInstance(this)
    }

    val repository: LuminaRepository by lazy {
        LuminaRepository(database, this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Lan chay truoc cua worker co the bi he thong dung giua chung (het pin, khoi dong lai
        // may). Dat lich lai khi mo app de bai con ket trong hang doi duoc tai len tiep.
        repository.resumePendingUploads()
        val loader = newImageLoader()
        coil.Coil.setImageLoader(loader)
        
        // Seed database on background thread immediately if needed
        DatabaseSeeder.seedDatabase(database)
    }

    override fun newImageLoader(): ImageLoader {
        if (customImageLoader != null) {
            return customImageLoader!!
        }
        val loader = ImageLoader.Builder(this)
            .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            .allowRgb565(false)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
        customImageLoader = loader
        return loader
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        try {
            if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
                customImageLoader?.memoryCache?.clear()
            }
        } catch (_: Exception) {}
    }

    override fun onLowMemory() {
        super.onLowMemory()
        try {
            customImageLoader?.memoryCache?.clear()
        } catch (_: Exception) {}
    }

    companion object {
        lateinit var instance: LuminaApplication
            private set
    }
}

