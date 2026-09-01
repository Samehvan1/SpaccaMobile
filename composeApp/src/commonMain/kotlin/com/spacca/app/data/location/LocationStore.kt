package com.spacca.app.data.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LocationStatus { IDLE, LOCATING, AVAILABLE, DENIED, ERROR }

class LocationStore(private val provider: LocationProvider) {
    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private val _status = MutableStateFlow(LocationStatus.IDLE)
    val status: StateFlow<LocationStatus> = _status.asStateFlow()

    suspend fun refresh() {
        _status.value = LocationStatus.LOCATING
        val loc = provider.getCurrentLocation()
        if (loc != null) {
            _location.value = loc
            _status.value = LocationStatus.AVAILABLE
        } else {
            _status.value = LocationStatus.ERROR
        }
    }
}
