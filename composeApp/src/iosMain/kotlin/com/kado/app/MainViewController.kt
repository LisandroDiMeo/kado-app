package com.kado.app

import androidx.compose.ui.window.ComposeUIViewController
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kado.app.data.local.MIGRATION_1_2
import com.kado.app.data.local.MIGRATION_2_3
import com.kado.app.data.local.MIGRATION_3_4
import com.kado.app.data.local.MIGRATION_4_5
import com.kado.app.data.local.MIGRATION_5_6
import com.kado.app.data.local.MIGRATION_6_7
import com.kado.app.data.local.getDatabaseBuilder
import com.kado.app.di.AppDependencies

private var databaseInitialized = false

fun MainViewController() = ComposeUIViewController {
    if (!databaseInitialized) {
        AppDependencies.database = getDatabaseBuilder()
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .setDriver(BundledSQLiteDriver())
            .build()
        databaseInitialized = true
    }
    App()
}
