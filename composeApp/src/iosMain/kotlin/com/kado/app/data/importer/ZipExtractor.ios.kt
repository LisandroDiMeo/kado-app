package com.kado.app.data.importer

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.z_stream

@OptIn(ExperimentalForeignApi::class)
actual object ZipExtractor {

    actual fun listEntries(zipBytes: ByteArray): List<String> {
        val entries = mutableListOf<String>()
        var offset = 0
        while (offset + 4 <= zipBytes.size) {
            val sig = readInt32LE(zipBytes, offset)
            if (sig != LOCAL_FILE_HEADER_SIG) break

            val compressedSize = readInt32LE(zipBytes, offset + 18)
            val filenameLen = readInt16LE(zipBytes, offset + 26)
            val extraLen = readInt16LE(zipBytes, offset + 28)
            val filename = zipBytes.decodeToString(offset + 30, offset + 30 + filenameLen)
            entries.add(filename)

            offset += 30 + filenameLen + extraLen + compressedSize
        }
        return entries
    }

    actual fun extractEntry(zipBytes: ByteArray, entryName: String): ByteArray? {
        var offset = 0
        while (offset + 4 <= zipBytes.size) {
            val sig = readInt32LE(zipBytes, offset)
            if (sig != LOCAL_FILE_HEADER_SIG) break

            val method = readInt16LE(zipBytes, offset + 8)
            val compressedSize = readInt32LE(zipBytes, offset + 18)
            val uncompressedSize = readInt32LE(zipBytes, offset + 22)
            val filenameLen = readInt16LE(zipBytes, offset + 26)
            val extraLen = readInt16LE(zipBytes, offset + 28)
            val filename = zipBytes.decodeToString(offset + 30, offset + 30 + filenameLen)

            val dataOffset = offset + 30 + filenameLen + extraLen

            if (filename == entryName) {
                val compressedData = zipBytes.copyOfRange(dataOffset, dataOffset + compressedSize)
                return when (method) {
                    0 -> compressedData // STORE
                    8 -> inflateData(compressedData, uncompressedSize) // DEFLATE
                    else -> null
                }
            }

            offset = dataOffset + compressedSize
        }
        return null
    }

    actual fun extractEntries(zipBytes: ByteArray, entryNames: Set<String>): Map<String, ByteArray> {
        val results = mutableMapOf<String, ByteArray>()
        val remaining = entryNames.toMutableSet()
        var offset = 0
        while (offset + 4 <= zipBytes.size && remaining.isNotEmpty()) {
            val sig = readInt32LE(zipBytes, offset)
            if (sig != LOCAL_FILE_HEADER_SIG) break

            val method = readInt16LE(zipBytes, offset + 8)
            val compressedSize = readInt32LE(zipBytes, offset + 18)
            val uncompressedSize = readInt32LE(zipBytes, offset + 22)
            val filenameLen = readInt16LE(zipBytes, offset + 26)
            val extraLen = readInt16LE(zipBytes, offset + 28)
            val filename = zipBytes.decodeToString(offset + 30, offset + 30 + filenameLen)

            val dataOffset = offset + 30 + filenameLen + extraLen

            if (filename in remaining) {
                val compressedData = zipBytes.copyOfRange(dataOffset, dataOffset + compressedSize)
                val extracted = when (method) {
                    0 -> compressedData
                    8 -> inflateData(compressedData, uncompressedSize)
                    else -> null
                }
                if (extracted != null) {
                    results[filename] = extracted
                }
                remaining.remove(filename)
            }

            offset = dataOffset + compressedSize
        }
        return results
    }

    private fun inflateData(compressed: ByteArray, expectedSize: Int): ByteArray {
        val output = ByteArray(expectedSize)

        compressed.usePinned { compressedPinned ->
            output.usePinned { outputPinned ->
                memScoped {
                    val stream = alloc<z_stream>()
                    stream.next_in = compressedPinned.addressOf(0).reinterpret<UByteVar>()
                    stream.avail_in = compressed.size.toUInt()
                    stream.next_out = outputPinned.addressOf(0).reinterpret<UByteVar>()
                    stream.avail_out = output.size.toUInt()

                    // -15 = raw deflate (no zlib/gzip header)
                    val initResult = inflateInit2(stream.ptr, -15)
                    if (initResult != Z_OK) {
                        throw IllegalStateException("inflateInit2 failed: $initResult")
                    }

                    val inflateResult = inflate(stream.ptr, Z_FINISH)
                    inflateEnd(stream.ptr)

                    if (inflateResult != Z_STREAM_END && inflateResult != Z_OK) {
                        throw IllegalStateException("inflate failed: $inflateResult")
                    }

                    val actualSize = output.size - stream.avail_out.toInt()
                    return output.copyOf(actualSize)
                }
            }
        }

        return output // unreachable but needed for compiler
    }

    private fun readInt16LE(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
                ((data[offset + 1].toInt() and 0xFF) shl 8)

    private fun readInt32LE(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                ((data[offset + 2].toInt() and 0xFF) shl 16) or
                ((data[offset + 3].toInt() and 0xFF) shl 24)

    private const val LOCAL_FILE_HEADER_SIG = 0x04034B50
}
