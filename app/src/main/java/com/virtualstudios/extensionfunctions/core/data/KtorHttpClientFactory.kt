package com.virtualstudios.extensionfunctions.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorHttpClientFactory {

    fun create(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine){
            install(ContentNegotiation){
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpTimeout){
                socketTimeoutMillis = 20_000L
                connectTimeoutMillis = 20_000L
                requestTimeoutMillis = 20_000L
            }
            install(Logging){
                logger = object : Logger { //use for cmp
                    override fun log(message: String) {
                        println(message)
                    }
                }
                //logger = Logger.ANDROID //use for android
                level = LogLevel.ALL
            }
            defaultRequest {
                contentType(ContentType.Application.Json)
            }
        }
    }
}