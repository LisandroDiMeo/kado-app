package com.kado.app.data.importer

expect object MediaStorage {
    fun saveMedia(deckId: Long, filename: String, bytes: ByteArray)
    fun getMediaPath(deckId: Long, filename: String): String
    fun deleteMediaDir(deckId: Long)
    fun deleteMedia(deckId: Long, filename: String)
    fun listMedia(deckId: Long): List<String>
}
