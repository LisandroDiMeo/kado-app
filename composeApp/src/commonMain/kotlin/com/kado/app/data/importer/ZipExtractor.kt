package com.kado.app.data.importer

expect object ZipExtractor {
    fun listEntries(zipBytes: ByteArray): List<String>
    fun extractEntry(zipBytes: ByteArray, entryName: String): ByteArray?
    fun extractEntries(zipBytes: ByteArray, entryNames: Set<String>): Map<String, ByteArray>
}
