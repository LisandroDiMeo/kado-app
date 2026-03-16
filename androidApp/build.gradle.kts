plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebaseCrashlytics)
}

val major = providers.gradleProperty("app.version.major").get().toInt()
val minor = providers.gradleProperty("app.version.minor").get().toInt()
val patch = providers.gradleProperty("app.version.patch").get().toInt()
val buildOffset = providers.gradleProperty("app.version.buildOffset").get().toInt()
val buildDest = providers.gradleProperty("app.version.buildDestination").get().toInt()

android {
    namespace = "com.eldiem.kado.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.eldiem.kado.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = buildDest * 100_000_000 + major * 1_000_000 + minor * 10_000 + patch * 100 + buildOffset
        versionName = "$major.$minor.$patch"
    }
    signingConfigs {
        create("release") {
            val ciKeystore = System.getenv("KEYSTORE_PATH")
            if (ciKeystore != null) {
                storeFile = file(ciKeystore)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    debugImplementation(libs.compose.uiTooling)
}
