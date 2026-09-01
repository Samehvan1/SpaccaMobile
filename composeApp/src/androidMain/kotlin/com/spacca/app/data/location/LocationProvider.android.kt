package com.spacca.app.data.location

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.spacca.app.data.cache.AndroidContextHolder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

actual class LocationProvider {
    actual suspend fun getCurrentLocation(): Location? {
        val ctx = AndroidContextHolder.context ?: return null
        val manager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        // 1) Try the most recent known location first (fast, no permission dialog needed if granted).
        val lastKnown = lastKnownLocation(manager)
        if (lastKnown != null) {
            return lastKnown.toCommon()
        }

        // 2) Otherwise request a single fresh fix with a timeout.
        return requestSingleFix(manager)
    }

    private fun lastKnownLocation(manager: LocationManager): android.location.Location? {
        return try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            providers
                .asSequence()
                .mapNotNull { provider ->
                    try {
                        manager.getLastKnownLocation(provider)
                    } catch (_: SecurityException) {
                        null
                    } catch (_: Exception) {
                        null
                    }
                }
                .firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun requestSingleFix(manager: LocationManager): Location? {
        return withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: android.location.Location) {
                        if (cont.isActive) cont.resume(location.toCommon())
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                val requested = try {
                    manager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        listener,
                        Looper.getMainLooper()
                    )
                    true
                } catch (_: SecurityException) {
                    false
                } catch (_: Exception) {
                    false
                }

                if (!requested) {
                    if (cont.isActive) cont.resume(null)
                    return@suspendCancellableCoroutine
                }

                cont.invokeOnCancellation {
                    try {
                        manager.removeUpdates(listener)
                    } catch (_: Exception) {
                        // ignore
                    }
                }
            }
        }
    }

    private fun android.location.Location.toCommon(): Location =
        Location(latitude = latitude, longitude = longitude)
}
