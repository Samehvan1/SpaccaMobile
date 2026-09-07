package com.spacca.app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiConfig {
    // Base URL of the SpaccaPos Express backend (mobile routes under /api/mobile).
    //
    // The app defaults to the production VPS. For local development/testing you
    // can switch to a local server at runtime from the "More" screen (the
    // selection is persisted and applied immediately). See EnvironmentStore.
    //
    // NOTE: Ktor resolves request paths against this base URL by REPLACING the
    // base path. So keep the base URL WITHOUT a path suffix and include the full
    // path (e.g. "/api/mobile/...") in each ApiService request.
    const val VPS_URL = "https://31-97-157-159.sslip.io"
    const val DEFAULT_LOCAL_URL = "http://127.0.0.1:8080"

    // Current base URL. This is a `var` so it can be switched at runtime. The
    // Ktor client resolves it per-request (defaultRequest's block is re-invoked
    // for every request and reads the current value), so changes take effect
    // immediately without recreating the client or restarting.
    var BASE_URL = VPS_URL
}

fun createHttpClient(engine: HttpClientEngine, cookieStorage: CookiesStorage): HttpClient {
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
        // The storage is persistent (disk-backed) so the session survives restarts.
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
        // Resolve relative request paths against the CURRENT ApiConfig.BASE_URL.
        // defaultRequest's block runs on every request and reads the mutable
        // BASE_URL, so switching environments (VPS <-> local) takes effect
        // immediately without recreating the client.
        defaultRequest {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }
    }
}
