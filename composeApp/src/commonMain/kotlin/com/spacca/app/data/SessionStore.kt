package com.spacca.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Tracks the customer's login state. The backend authenticates via session
// cookies (stored automatically by Ktor's HttpCookies plugin), so we don't
// manage a token here — only the UI-facing session state.
class SessionStore {
    private val _customerId = MutableStateFlow<Int?>(null)
    val customerId: StateFlow<Int?> = _customerId.asStateFlow()

    private val _phone = MutableStateFlow<String?>(null)
    val phone: StateFlow<String?> = _phone.asStateFlow()

    private val _hasPin = MutableStateFlow<Boolean?>(null)
    val hasPin: StateFlow<Boolean?> = _hasPin.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun setSession(customerId: Int?, phone: String?, hasPin: Boolean?) {
        _customerId.value = customerId
        _phone.value = phone
        _hasPin.value = hasPin
        _isLoggedIn.value = customerId != null
    }

    fun setHasPin(hasPin: Boolean) {
        _hasPin.value = hasPin
    }

    fun clear() {
        _customerId.value = null
        _phone.value = null
        _hasPin.value = null
        _isLoggedIn.value = false
    }
}
