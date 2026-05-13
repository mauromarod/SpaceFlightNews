plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.mauromarod.spaceflightnews.core.common"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
