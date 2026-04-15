package com.kado.app.data.importer

import com.kado.app.data.local.appContext
import java.io.File

actual object MediaStorage {
    actual fun saveMedia(deckId: Long, filename: String, bytes: ByteArray) {
        val dir = File(appContext.filesDir, "media/$deckId")
        dir.mkdirs()
        File(dir, filename).writeBytes(bytes)
    }

    actual fun getMediaPath(deckId: Long, filename: String): String =
        File(appContext.filesDir, "media/$deckId/$filename").absolutePath

    actual fun deleteMediaDir(deckId: Long) {
        val dir = File(appContext.filesDir, "media/$deckId")
        if (dir.exists()) dir.deleteRecursively()
    }

    actual fun deleteMedia(deckId: Long, filename: String) {
        val file = File(appContext.filesDir, "media/$deckId/$filename")
        if (file.exists()) file.delete()
    }

    actual fun listMedia(deckId: Long): List<String> {
        val dir = File(appContext.filesDir, "media/$deckId")
        return dir.listFiles()?.map { it.name } ?: emptyList()
    }
}
