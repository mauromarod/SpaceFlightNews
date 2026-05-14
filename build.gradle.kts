plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.androidx.baselineprofile) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.firebase.crashlytics") version "3.0.3" apply false
    id("com.google.firebase.appdistribution") version "5.2.1" apply false
    jacoco
}

val excluded = listOf(
    // Android generated
    "**/R.class", "**/R\$*.class", "**/BuildConfig.class",
    // Hilt / Dagger generated
    "**/*_Factory*", "**/*HiltModules*", "**/Hilt_*",
    "**/hilt_aggregated_deps/**", "**/dagger/**",
    "**/*MembersInjector*", "**/*_Impl*",
    // Compose generated
    "**/*ComposableSingletons*",
    // Compose UI — Screen files and previews (no unit-testable logic)
    "**/*Screen*", "**/*Preview*",
    // Moshi generated
    "**/*JsonAdapter*",
    // Domain repository interfaces — pure contracts, no logic to unit-test
    "**/spaceflightnews/core/domain/repository/**",
    // Stage-14 auth/preferences models — no auth unit tests in scope
    "**/model/AuthUser*", "**/model/ThemePreference*", "**/model/LanguagePreference*",
    // Sealed UI contracts — data containers verified via ViewModel behavior, no own logic
    "**/*UiState*", "**/*UiEvent*", "**/*UiEffect*",
    // MockK-generated subclasses recorded in exec files — not real production classes
    "**/*\$Subclass*", "**/*\$auxiliary*",
)

tasks.register<JacocoReport>("jacocoCoverageReport") {
    group = "verification"
    description = "Unified JaCoCo coverage report for all modules"

    dependsOn(
        ":core:domain:test",
        ":core:data:testDebugUnitTest",
        ":features:news:testDebugUnitTest",
        ":features:detail:testDebugUnitTest",
    )

    reports {
        html.required.set(true)
        xml.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
    }

    executionData.setFrom(
        fileTree("${project(":core:domain").buildDir}/jacoco") { include("**/*.exec") },
        fileTree("${project(":core:data").buildDir}/outputs/unit_test_code_coverage/debugUnitTest") { include("**/*.exec") },
        fileTree("${project(":features:news").buildDir}/outputs/unit_test_code_coverage/debugUnitTest") { include("**/*.exec") },
        fileTree("${project(":features:detail").buildDir}/outputs/unit_test_code_coverage/debugUnitTest") { include("**/*.exec") },
    )

    classDirectories.setFrom(
        // core:domain — include only usecase + model (excluding Stage-14 auth models)
        fileTree("${project(":core:domain").buildDir}/classes/kotlin/main") {
            include("**/usecase/**", "**/model/**")
            exclude("**/model/AuthUser*", "**/model/ThemePreference*", "**/model/LanguagePreference*")
        },
        fileTree("${project(":core:data").buildDir}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") { exclude(excluded) },
        fileTree("${project(":features:news").buildDir}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") { exclude(excluded) },
        fileTree("${project(":features:detail").buildDir}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") { exclude(excluded) },
    )

    sourceDirectories.setFrom(
        files(
            "${project(":core:domain").projectDir}/src/main/kotlin",
            "${project(":core:data").projectDir}/src/main/kotlin",
            "${project(":features:news").projectDir}/src/main/kotlin",
            "${project(":features:detail").projectDir}/src/main/kotlin",
        )
    )
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
    }
}

