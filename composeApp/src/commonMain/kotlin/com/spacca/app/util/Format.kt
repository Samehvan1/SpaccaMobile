package com.spacca.app.util

import kotlin.math.absoluteValue
import kotlin.math.roundToLong

/**
 * Formats a Double as a price with exactly 2 decimals, e.g. 12.5 -> "12.50".
 * Cross-platform replacement for the JVM-only `"%.2f".format(...)`.
 */
fun Double.formatPrice(): String {
    val rounded = (this * 100).roundToLong()
    val whole = rounded / 100
    val frac = (rounded % 100).toInt().absoluteValue
    return "$whole.${frac.toString().padStart(2, '0')}"
}

/**
 * Formats a Double as a whole number (0 decimals), e.g. 12.7 -> "13".
 * Cross-platform replacement for the JVM-only `"%.0f".format(...)`.
 */
fun Double.formatWhole(): String = roundToLong().toString()

/**
 * Formats hour/minute as zero-padded "HH:mm", e.g. 9, 5 -> "09:05".
 * Cross-platform replacement for the JVM-only `"%02d:%02d".format(...)`.
 */
fun formatTime(hour: Int, minute: Int): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
