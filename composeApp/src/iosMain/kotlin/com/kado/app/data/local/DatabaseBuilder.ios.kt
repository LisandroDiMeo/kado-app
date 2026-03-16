package com.kado.app.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun getDatabaseBuilder(): RoomDatabase.Builder<KadoDatabase> {
    val paths = NSFileManager.defaultManager.URLsForDirectory(
        NSDocumentDirectory,
        NSUserDomainMask
    )
    val documentsDir = (paths.first() as NSURL).path!!
    val dbPath = "$documentsDir/kado.db"
    return Room.databaseBuilder<KadoDatabase>(name = dbPath)
}
