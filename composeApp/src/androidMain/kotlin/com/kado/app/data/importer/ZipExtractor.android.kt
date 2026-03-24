package com.kado.app.data.importer

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

actual object ZipExtractor {
    actual fun listEntries(zipBytes: ByteArray): List<String> {
        val entries = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return entries
    }

    actual fun extractEntry(zipBytes: ByteArray, entryName: String): ByteArray? {
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == entryName) {
                    val baos = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (zis.read(buffer).also { len = it } != -1) {
                        baos.write(buffer, 0, len)
                    }
                    return baos.toByteArray()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return null
    }

    actual fun extractEntries(zipBytes: ByteArray, entryNames: Set<String>): Map<String, ByteArray> {
        val results = mutableMapOf<String, ByteArray>()
        val remaining = entryNames.toMutableSet()
        ZipInputStream(ByteArrayInputStream(zipBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null && remaining.isNotEmpty()) {
                if (entry.name in remaining) {
                    val baos = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (zis.read(buffer).also { len = it } != -1) {
                        baos.write(buffer, 0, len)
                    }
                    results[entry.name] = baos.toByteArray()
                    remaining.remove(entry.name)
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        return results
    }
}
