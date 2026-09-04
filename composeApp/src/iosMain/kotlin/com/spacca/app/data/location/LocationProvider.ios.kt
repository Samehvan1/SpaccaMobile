package com.spacca.app.data.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.Foundation.NSError
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
actual class LocationProvider {

    @OptIn(ExperimentalForeignApi::class)
    private class Delegate(
        private val onResult: (CLLocation?) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        private var manager: CLLocationManager? = null

        fun attach(mgr: CLLocationManager) {
            manager = mgr
        }

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateToLocation: CLLocation,
            fromLocation: CLLocation
        ) {
            manager.stopUpdatingLocation()
            onResult(didUpdateToLocation)
        }

        override fun locationManager(
            manager: CLLocationManager,
            didFailWithError: NSError?
        ) {
            manager.stopUpdatingLocation()
            onResult(null)
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            val status = manager.authorizationStatus()
            if (status == kCLAuthorizationStatusDenied ||
                status == kCLAuthorizationStatusRestricted
            ) {
                manager.stopUpdatingLocation()
                onResult(null)
            } else if (status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                status == kCLAuthorizationStatusAuthorizedAlways
            ) {
                manager.startUpdatingLocation()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): Location? {
        val manager = CLLocationManager()
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                val delegate = Delegate { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
                delegate.attach(manager)
                manager.delegate = delegate

                val status = manager.authorizationStatus()
                if (status == kCLAuthorizationStatusNotDetermined) {
                    manager.requestWhenInUseAuthorization()
                } else if (status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways
                ) {
                    manager.startUpdatingLocation()
                } else {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }?.let { loc ->
            val lat = loc.coordinate.useContents { latitude }
            val lng = loc.coordinate.useContents { longitude }
            Location(latitude = lat, longitude = lng)
        }
    }
}
