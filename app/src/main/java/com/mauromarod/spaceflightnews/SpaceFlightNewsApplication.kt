package com.mauromarod.spaceflightnews

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.StrictMode
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import okio.Path.Companion.toOkioPath

@HiltAndroidApp
class SpaceFlightNewsApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.02)
                    .build()
            }
            .build()
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectLeakedSqlLiteObjects()
            .detectLeakedClosableObjects()
            .detectContentUriWithoutPermission()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vmPolicyBuilder.detectUnsafeIntentLaunch()
        }
        vmPolicyBuilder
            .penaltyLog()
            .build()
            .let { StrictMode.setVmPolicy(it) }
    }
}
