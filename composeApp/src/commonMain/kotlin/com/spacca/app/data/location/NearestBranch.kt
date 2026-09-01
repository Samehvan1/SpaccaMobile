package com.spacca.app.data.location

import com.spacca.app.data.model.Branch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Haversine distance in kilometers between two lat/lng points. */
fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

/** Returns the branch nearest to the given location, or null if none have coordinates. */
fun nearestBranch(branches: List<Branch>, location: Location): Branch? {
    return branches
        .filter { it.latitude != null && it.longitude != null }
        .minByOrNull { haversineKm(location.latitude, location.longitude, it.latitude!!, it.longitude!!) }
}
