package com.spacca.app.data.location

data class Location(
    val latitude: Double,
    val longitude: Double
)

/** Returns the device's current location, or null if unavailable/denied. */
expect class LocationProvider() {
    suspend fun getCurrentLocation(): Location?
}
