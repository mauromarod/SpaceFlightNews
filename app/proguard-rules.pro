# Keep line numbers for readable crash reports in Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep generic signatures — required by NetworkResultCallAdapterFactory (Retrofit call adapter
# inspects ParameterizedType at runtime; R8 strips Signature without this rule)
-keepattributes Signature

# Retrofit
-keepattributes Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Moshi — keep generated JsonAdapters and all DTO classes
-keep class com.mauromarod.spaceflightnews.core.network.dto.** { *; }
-keep class **JsonAdapter { *; }
-keepclassmembers class **JsonAdapter {
    <init>(...);
    <fields>;
}

# NetworkResult sealed class — keep all subclasses so the call adapter can inspect them
-keep class com.mauromarod.spaceflightnews.core.network.NetworkResult { *; }
-keep class com.mauromarod.spaceflightnews.core.network.NetworkResult$* { *; }

# Room — keep entity and DAO classes
-keep class com.mauromarod.spaceflightnews.core.database.entity.** { *; }
-keep class com.mauromarod.spaceflightnews.core.database.dao.** { *; }

# Coil 3 — keep all classes so ServiceLoader can resolve NetworkFetcher via META-INF/services
# Without this, R8 obfuscates class names and coil-network-okhttp's fetcher is never registered
-keep class coil3.** { *; }
-keepnames class coil3.**
-dontwarn coil3.**

# OkHttp — used by coil-network-okhttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-keep class kotlin.Metadata { *; }
# R8 full mode strips internal coroutine state machine classes; dontwarn suppresses false positives
-dontwarn kotlinx.coroutines.flow.**

# DataStore Preferences — keys are resolved by name via string constants, no reflection needed.
# The Preferences proto schema is compiled into the app; only the outer class needs keeping.
-keep class androidx.datastore.preferences.core.** { *; }

# Hilt — generated component classes must survive obfuscation so the DI graph is resolvable
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembernames class * {
    @dagger.hilt.* <methods>;
}

# Firebase Performance — trace names are resolved by string at runtime
-keep class com.google.firebase.perf.** { *; }
-dontwarn com.google.firebase.perf.**

# Firebase Auth — user model accessed via reflection by the SDK
-keep class com.google.firebase.auth.** { *; }

# Firebase Remote Config — accessed by string key at runtime
-keep class com.google.firebase.remoteconfig.** { *; }
