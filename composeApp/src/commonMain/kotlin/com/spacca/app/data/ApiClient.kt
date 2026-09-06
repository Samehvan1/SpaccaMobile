package com.spacca.app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
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
    const val DEFAULT_LOCAL_URL = "http://192.168.1.19:8080"

    // Current base URL. This is a `var` so it can be switched at runtime; the
    // Ktor client resolves it per-request (see dynamicBaseUrl plugin), so changes
    // take effect immediately without recreating the client or restarting.
    var BASE_URL = VPS_URL
}

/**
 * Resolves relative request URLs (e.g. "/api/mobile/...") against the *current*
 * [ApiConfig.BASE_URL] on every request. This lets the base URL be switched at
 * runtime (VPS <-> local) without recreating the HttpClient, because Ktor's
 * defaultRequest would otherwise capture the base URL only once at client
 * creation time.
 */
private val dynamicBaseUrl = createClientPlugin("DynamicBaseUrl") {
    onRequest { request, _ ->
        if (request.url.host.isEmpty()) {
            val base = ApiConfig.BASE_URL.trimEnd('/')
            val built = request.url.build()
            val path = built.encodedPath
            val query = built.encodedQuery
            request.url.takeFrom(base + path + if (query.isNotEmpty()) "?$query" else "")
        }
    }
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
        // Resolve relative request paths against the current ApiConfig.BASE_URL
        // on every request, so the environment can be switched at runtime.
        install(dynamicBaseUrl)
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}
