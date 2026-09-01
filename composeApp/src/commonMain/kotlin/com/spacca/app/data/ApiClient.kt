package com.spacca.app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiConfig {
    // Base URL of the SpaccaPos Express backend (mobile routes under /api/mobile)
    //
    // NOTE: 10.0.2.2 is the Android *emulator's* alias for the host machine.
    // On a physical device this does NOT route to the dev machine, so set this
    // to the dev machine's LAN IP (e.g. http://192.168.x.x:3000/api) instead.
    // This is a `var` so it can be overridden at runtime for device testing.
    //
    // For a USB-connected physical device, use adb reverse so the device's
    // localhost:8080 forwards to the host's backend:
    //   adb reverse tcp:8080 tcp:8080
    // then point BASE_URL at 127.0.0.1:8080.
    //
    // NOTE: Ktor's defaultRequest resolves request paths against this base URL
    // by REPLACING the base path. So keep the base URL WITHOUT a path suffix and
    // include the full path (e.g. "/api/mobile/...") in each ApiService request.
    var BASE_URL = "http://127.0.0.1:8080"
}

fun createHttpClient(engine: HttpClientEngine): HttpClient {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(json)
        }
        // The backend authenticates via session cookies (req.session.customerId).
        // HttpCookies automatically stores the session cookie and sends it on
        // subsequent requests, so no manual token management is required.
        install(HttpCookies)
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        defaultRequest {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}
