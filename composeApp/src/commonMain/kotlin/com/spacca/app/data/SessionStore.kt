package com.spacca.app.data

import com.spacca.app.data.cache.CacheStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Tracks the customer's login state. The backend authenticates via session
// cookies (stored automatically by Ktor's HttpCookies plugin), so we don't
// manage a token here — only the UI-facing session state.
//
// The state is persisted to disk via CacheStore so login survives app
// restarts (until logout or backend session expiry).
class SessionStore(private val cacheStore: CacheStore) {
    private val key = "session_state"
    private val json = Json { ignoreUnknownKeys = true }

    private val _customerId = MutableStateFlow<Int?>(null)
    val customerId: StateFlow<Int?> = _customerId.asStateFlow()

    private val _phone = MutableStateFlow<String?>(null)
    val phone: StateFlow<String?> = _phone.asStateFlow()

    private val _hasPin = MutableStateFlow<Boolean?>(null)
    val hasPin: StateFlow<Boolean?> = _hasPin.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        load()
    }

    fun setSession(customerId: Int?, phone: String?, hasPin: Boolean?) {
        _customerId.value = customerId
        _phone.value = phone
        _hasPin.value = hasPin
        _isLoggedIn.value = customerId != null
        persist()
    }

    fun setHasPin(hasPin: Boolean) {
        _hasPin.value = hasPin
        persist()
    }

    /** Clears the active session but keeps phone + hasPin (used when the backend session expires). */
    fun clearSession() {
        _customerId.value = null
        _isLoggedIn.value = false
        persist()
    }

    /** Full logout: clears everything including the remembered phone/hasPin. */
    fun clear() {
        _customerId.value = null
        _phone.value = null
        _hasPin.value = null
        _isLoggedIn.value = false
        persist()
    }

    private fun load() {
        val raw = cacheStore.get(key) ?: return
        try {
            val s = json.decodeFromString<SessionState>(raw)
            _customerId.value = s.customerId
            _phone.value = s.phone
            _hasPin.value = s.hasPin
            _isLoggedIn.value = s.customerId != null
        } catch (e: Exception) {
            // ignore corrupt state
        }
    }

    private fun persist() {
        val s = SessionState(_customerId.value, _phone.value, _hasPin.value)
        cacheStore.put(key, json.encodeToString(SessionState.serializer(), s))
    }
}

@Serializable
private data class SessionState(
    val customerId: Int? = null,
    val phone: String? = null,
    val hasPin: Boolean? = null
)