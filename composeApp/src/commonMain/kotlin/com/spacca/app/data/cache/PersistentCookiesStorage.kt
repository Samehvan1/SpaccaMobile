package com.spacca.app.data.cache

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import kotlinx.serialization.json.Json

/**
 * CookiesStorage that persists cookies to disk via [CacheStore] so the backend
 * session cookie survives app restarts. The backend authenticates via a
 * session cookie (express-session, Postgres-backed, 24h maxAge), so keeping the
 * cookie across restarts keeps the user logged in until logout or expiry.
 *
 * Ktor 3.x `Cookie` is @Serializable, so the whole jar is stored as JSON.
 *
 * Cookies are matched by domain + path (like Ktor's AcceptAllCookiesStorage),
 * not by exact request URL, so a cookie set on one path is sent on others.
 */
class PersistentCookiesStorage(
    private val cacheStore: CacheStore,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : CookiesStorage {

    private val key = "session_cookies"
    private val cookies = mutableListOf<Cookie>()

    init {
        load()
    }

    override suspend fun get(requestUrl: Url): List<Cookie> =
        cookies.filter { it.matches(requestUrl) }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
        cookies.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
        cookies.add(cookie)
        persist()
    }

    /** Removes all stored cookies (used on logout / session expiry). */
    fun clear() {
        cookies.clear()
        persist()
    }

    override fun close() = Unit

    private fun load() {
        val raw = cacheStore.get(key) ?: return
        try {
            cookies.clear()
            cookies.addAll(json.decodeFromString<List<Cookie>>(raw))
        } catch (e: Exception) {
            cookies.clear()
        }
    }

    private fun persist() {
        try {
            cacheStore.put(key, json.encodeToString(cookies))
        } catch (e: Exception) {
            // Cache writes must never crash the app.
        }
    }
}

/** Domain + path matching, mirroring Ktor's Cookie.matches. */
private fun Cookie.matches(requestUrl: Url): Boolean {
    val host = requestUrl.host.lowercase()
    // A null domain means the cookie is host-only (matches only the exact host).
    val domain = domain?.lowercase()?.trimStart('.') ?: host
    val path = path?.let { if (it.endsWith('/')) it else "$it/" } ?: return false

    val requestPath = requestUrl.encodedPath.let { if (it.endsWith('/')) it else "$it/" }

    if (host != domain && (host.isIp() || !host.endsWith(".$domain"))) return false
    if (path != "/" && requestPath != path && !requestPath.startsWith(path)) return false
    val secureProtocol = requestUrl.protocol.name == "https" || requestUrl.protocol.name == "wss"
    return !(secure && !secureProtocol)
}

private fun String.isIp(): Boolean {
    // IPv4
    val parts = split('.')
    if (parts.size == 4 && parts.all { it.toIntOrNull()?.let { n -> n in 0..255 } == true }) return true
    // IPv6 (contains ':')
    return contains(':')
}