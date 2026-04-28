package com.kado.app.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kado.app.data.wifi.WifiConnector
import com.kado.app.di.AppDependencies

lateinit var appContext: Context
    internal set

fun initDatabase(context: Context) {
    appContext = context.applicationContext
}

actual fun getDatabaseBuilder(): RoomDatabase.Builder<KadoDatabase> {
    val dbFile = appContext.getDatabasePath("kado.db")
    return Room.databaseBuilder<KadoDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

fun initApp(context: Context) {
    initDatabase(context)
    AppDependencies.database = getDatabaseBuilder()
        .addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        )
        .setDriver(BundledSQLiteDriver())
        .build()
    AppDependencies.wifiConnector = WifiConnector(context.applicationContext)
}
