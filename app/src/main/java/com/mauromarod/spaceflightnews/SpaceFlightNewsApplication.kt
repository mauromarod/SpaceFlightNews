package com.mauromarod.spaceflightnews

import android.app.Application
import android.os.Build
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SpaceFlightNewsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }
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
