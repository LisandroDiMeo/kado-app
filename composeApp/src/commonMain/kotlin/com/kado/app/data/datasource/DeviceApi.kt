package com.kado.app.data.datasource

import com.kado.app.domain.repository.DeviceDeck
import com.kado.app.domain.repository.DeviceDecksResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeviceApi {
    companion object {
        private const val BASE_URL = "http://192.168.4.1"
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    private data class DecksJson(
        val decks: List<DeckJson>,
        @SerialName("free_mb") val freeMb: Float
    )

    @Serializable
    private data class DeckJson(
        val name: String,
        val cards: Int
    )

    suspend fun getDecks(): DeviceDecksResponse {
        val response = client.get("$BASE_URL/api/decks").body<DecksJson>()
        return DeviceDecksResponse(
            decks = response.decks.map { DeviceDeck(it.name, it.cards) },
            freeMb = response.freeMb
        )
    }

    suspend fun uploadDeck(aldBytes: ByteArray, filename: String): Boolean {
        val response = client.submitFormWithBinaryData(
            url = "$BASE_URL/upload",
            formData = formData {
                append("file", aldBytes, Headers.build {
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }
        )
        return response.status.value in 200..299
    }

    suspend fun deleteDeck(index: Int): Boolean {
        val response = client.post("$BASE_URL/api/delete?idx=$index")
        return response.status.value in 200..299
    }
}
