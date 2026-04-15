import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKmpLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

val appVersionName: String = providers.gradleProperty("app.version.major").get() + "." +
    providers.gradleProperty("app.version.minor").get() + "." +
    providers.gradleProperty("app.version.patch").get()

val generateAppInfo = tasks.register("generateAppInfo") {
    val outputDir = layout.buildDirectory.dir("generated/appinfo/commonMain/kotlin")
    val version = appVersionName
    inputs.property("version", version)
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile.resolve("com/kado/app")
        dir.mkdirs()
        dir.resolve("AppInfo.kt").writeText(
            """
            |package com.kado.app
            |
            |object AppInfo {
            |    const val VERSION = "$version"
            |}
            """.trimMargin() + "\n"
        )
    }
}

kotlin {
    androidLibrary {
        namespace = "com.kado.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        androidResources.enable = true
        withHostTestBuilder {}
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(
                generateAppInfo.map {
                    layout.buildDirectory.dir("generated/appinfo/commonMain/kotlin").get()
                }
            )
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.coil.compose)
            implementation(libs.coil.svg)
            implementation(libs.coil.network.ktor3)
            implementation(libs.paging.common)
            implementation(libs.paging.compose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

ktlint {
    version.set("1.5.0")
    android.set(true)
    outputToConsole.set(true)
}

detekt {
    config.setFrom(files("${rootProject.projectDir}/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
    baseline = file("detekt-baseline.xml")
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
        "src/commonTest/kotlin"
    )
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.ComposableSingletons*",
                    "com.kado.app.di.*",
                    "com.kado.app.ui.theme.*",
                    "com.kado.app.AppInfo",
                    "com.kado.app.MainViewController*",
                    "com.kado.app.Platform*"
                )
                annotatedBy("androidx.compose.runtime.Composable")
            }
        }
        verify {
            rule {
                minBound(14)
            }
        }
    }
}
