package com.mauromarod.spaceflightnews.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mauromarod.spaceflightnews.core.domain.repository.CrashReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCrashReporter @Inject constructor(
    private val crashlytics: FirebaseCrashlytics,
) : CrashReporter {

    override fun recordNonFatal(throwable: Throwable, extras: Map<String, String>) {
        extras.forEach { (key, value) -> crashlytics.setCustomKey(key, value) }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setUserId(uid: String) {
        crashlytics.setUserId(uid)
    }
}
