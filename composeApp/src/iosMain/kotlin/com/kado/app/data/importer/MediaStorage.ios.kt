package com.kado.app.data.importer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual object MediaStorage {
    private fun mediaDir(deckId: Long): String {
        val docs = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).first() as String
        return "$docs/media/$deckId"
    }

    actual fun saveMedia(deckId: Long, filename: String, bytes: ByteArray) {
        val dir = mediaDir(deckId)
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(dir)) {
            fm.createDirectoryAtPath(dir, withIntermediateDirectories = true, attributes = null, error = null)
        }
        val path = "$dir/$filename"
        val data = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        data.writeToFile(path, atomically = true)
    }

    actual fun getMediaPath(deckId: Long, filename: String): String {
        return "${mediaDir(deckId)}/$filename"
    }

    actual fun deleteMediaDir(deckId: Long) {
        val dir = mediaDir(deckId)
        NSFileManager.defaultManager.removeItemAtPath(dir, null)
    }

    actual fun deleteMedia(deckId: Long, filename: String) {
        val path = "${mediaDir(deckId)}/$filename"
        NSFileManager.defaultManager.removeItemAtPath(path, null)
    }

    @Suppress("UNCHECKED_CAST")
    actual fun listMedia(deckId: Long): List<String> {
        val dir = mediaDir(deckId)
        val fm = NSFileManager.defaultManager
        if (!fm.fileExistsAtPath(dir)) return emptyList()
        return (fm.contentsOfDirectoryAtPath(dir, null) as? List<String>) ?: emptyList()
    }
}
