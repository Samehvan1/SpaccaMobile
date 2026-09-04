package com.spacca.app.data.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted

/**
 * iOS implementation of [LocationProvider].
 *
 * Uses a polling approach on [CLLocationManager.location] instead of overriding
 * the CLLocationManagerDelegate location-update callbacks. The delegate method
 * names exposed by the Kotlin/Native platform libraries vary between SDK/Xcode
 * versions (didUpdateLocations vs the deprecated didUpdateToLocation), which
 * makes overriding them fragile across CI runners. Polling the manager's
 * `location` property avoids that dependency entirely.
 */
@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): Location? {
        val manager = CLLocationManager()

        // Request authorization if not yet determined, waiting for the user's choice.
        val status = manager.authorizationStatus()
        if (status == kCLAuthorizationStatusNotDetermined) {
            manager.requestWhenInUseAuthorization()
            withTimeoutOrNull(10_000) {
                while (manager.authorizationStatus() == kCLAuthorizationStatusNotDetermined) {
                    delay(100)
                }
            }
        }

        val finalStatus = manager.authorizationStatus()
        if (finalStatus != kCLAuthorizationStatusAuthorizedWhenInUse &&
            finalStatus != kCLAuthorizationStatusAuthorizedAlways
        ) {
            return null
        }

        // Start updating and poll for a location fix.
        manager.startUpdatingLocation()
        val loc = withTimeoutOrNull(10_000) {
            while (manager.location == null) {
                delay(100)
            }
            manager.location
        }
        manager.stopUpdatingLocation()

        return loc?.let { l ->
            val lat = l.coordinate.useContents { latitude }
            val lng = l.coordinate.useContents { longitude }
            Location(latitude = lat, longitude = lng)
        }
    }
}
