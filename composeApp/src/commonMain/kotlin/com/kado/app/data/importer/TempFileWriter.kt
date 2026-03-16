package com.kado.app.data.importer

expect object TempFileWriter {
    fun writeTempFile(bytes: ByteArray, filename: String): String
    fun deleteTempFile(path: String)
}
