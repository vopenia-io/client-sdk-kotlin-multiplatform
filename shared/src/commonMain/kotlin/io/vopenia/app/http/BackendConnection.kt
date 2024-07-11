package io.vopenia.app.http

import eu.codlab.http.Configuration
import eu.codlab.http.createClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class BackendConnection(val url: String) {
    private val client = createClient(
        configuration = Configuration(
            enableLogs = true
        )
    )

    suspend fun token(participant: String, room: String): TokenAnswer {
        val result = client.post(
            "https://jwwfzpmlm3.execute-api.us-east-1.amazonaws.com",
        ) {
            contentType(ContentType.Application.Json)
            setBody(TokenBody(participant, room))
        }

        if (result.status.isSuccess()) {
            return result.body()
        }
        throw IllegalStateException("Invalid result ${result.status}")
    }
}
