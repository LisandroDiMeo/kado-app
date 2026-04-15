plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover) apply false
}

tasks.register("updateIosVersion") {
    description = "Updates iOS Config.xcconfig with version from gradle.properties"

    val major = providers.gradleProperty("app.version.major").get().toInt()
    val minor = providers.gradleProperty("app.version.minor").get().toInt()
    val patch = providers.gradleProperty("app.version.patch").get().toInt()
    val buildOffset = providers.gradleProperty("app.version.buildOffset").get().toInt()
    val buildDest = providers.gradleProperty("app.version.buildDestination").get().toInt()

    val versionName = "$major.$minor.$patch"
    val versionCode = buildDest * 100_000_000 +
        major * 1_000_000 + minor * 10_000 + patch * 100 + buildOffset

    val xcconfigFile = layout.projectDirectory.file("iosApp/Configuration/Config.xcconfig")

    doLast {
        xcconfigFile.asFile.writeText(
            """
            |TEAM_ID=
            |
            |PRODUCT_NAME=kado
            |PRODUCT_BUNDLE_IDENTIFIER=com.kado.app.kado${'$'}(TEAM_ID)
            |
            |CURRENT_PROJECT_VERSION=$versionCode
            |MARKETING_VERSION=$versionName
            """.trimMargin() + "\n"
        )
        println("Updated Config.xcconfig: version=$versionName, build=$versionCode")
    }
}
