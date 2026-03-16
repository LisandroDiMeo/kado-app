package com.kado.app.data.importer

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual object TempFileWriter {
    actual fun writeTempFile(bytes: ByteArray, filename: String): String {
        val tempDir = NSTemporaryDirectory()
        val path = "$tempDir$filename"
        val data = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        data.writeToFile(path, atomically = true)
        return path
    }

    actual fun deleteTempFile(path: String) {
        NSFileManager.defaultManager.removeItemAtPath(path, null)
    }
}
