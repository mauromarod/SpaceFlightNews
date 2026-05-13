plugins {
    alias(libs.plugins.kotlin.jvm)
    jacoco
}

kotlin {
    jvmToolchain(11)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(false)
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    // PagingData is allowed here — paging-common has no Android framework dependency
    api(libs.androidx.paging.common)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.archunit)
    testImplementation(libs.kotlinx.coroutines.test)
}
