plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.mauromarod.spaceflightnews.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        buildConfigField("Boolean", "DEBUG", "false")
    }

    buildTypes {
        debug {
            buildConfigField("Boolean", "DEBUG", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi.converter)
    implementation(libs.okhttp)
    implementation(libs.moshi)
    implementation(libs.kotlinx.coroutines.android)

    ksp(libs.moshi.kotlin.codegen)

    debugImplementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
