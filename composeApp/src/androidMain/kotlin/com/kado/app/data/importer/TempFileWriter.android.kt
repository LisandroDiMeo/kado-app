package com.kado.app.data.importer

import com.kado.app.data.local.appContext
import java.io.File

actual object TempFileWriter {
    actual fun writeTempFile(bytes: ByteArray, filename: String): String {
        val file = File(appContext.cacheDir, filename)
        file.writeBytes(bytes)
        return file.absolutePath
    }

    actual fun deleteTempFile(path: String) {
        File(path).delete()
    }
}
