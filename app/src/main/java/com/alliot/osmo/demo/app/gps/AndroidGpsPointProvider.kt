package com.alliot.osmo.demo.app.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.alliot.osmo.demo.session.model.SessionGpsPoint
import java.util.concurrent.Executor

class AndroidGpsPointProvider(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val locationManager = appContext.getSystemService(LocationManager::class.java)
    @Volatile
    private var cachedPoint: SessionGpsPoint? = null
    @Volatile
    private var lastActiveRequestAtMs: Long = 0L
    @Volatile
    private var activeRequestMinIntervalMs: Long = DEFAULT_ACTIVE_REQUEST_MIN_INTERVAL_MS

    @SuppressLint("MissingPermission")
    fun latestPoint(): SessionGpsPoint? {
        if (locationManager == null || !hasFineLocationPermission()) return null
        maybeRequestCurrentLocation()
        val now = System.currentTimeMillis()
        val candidates = listOfNotNull(
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER),
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
            locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER),
        )
        val best = selectBestLocation(candidates, now) ?: return cachedPoint?.takeIf { isFreshCachedPoint(it, now) }
        val point = best.toSessionGpsPoint()
        cachedPoint = point
        return point
    }

    fun setActiveRequestIntervalMs(intervalMs: Long) {
        activeRequestMinIntervalMs = intervalMs.coerceIn(250L, 5_000L)
    }

    @SuppressLint("MissingPermission")
    private fun maybeRequestCurrentLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val now = System.currentTimeMillis()
        if (now - lastActiveRequestAtMs < activeRequestMinIntervalMs) return
        lastActiveRequestAtMs = now
        locationManager?.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            null,
            DirectExecutor,
        ) { location ->
            if (location != null) {
                cachedPoint = location.toSessionGpsPoint()
            }
        }
    }

    private fun hasFineLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun selectBestLocation(candidates: List<Location>, nowMs: Long): Location? {
        return candidates
            .filter { isUsableLocation(it, nowMs) }
            .minByOrNull { locationScore(it, nowMs) }
    }

    private fun isUsableLocation(location: Location, nowMs: Long): Boolean {
        val ageMs = (nowMs - location.time).coerceAtLeast(0L)
        if (ageMs > MAX_LOCATION_AGE_MS) return false
        val accuracyMeters = location.accuracy
        if (accuracyMeters.isNaN() || accuracyMeters <= 0f) return false
        return accuracyMeters <= MAX_ACCEPTABLE_ACCURACY_METERS
    }

    private fun locationScore(location: Location, nowMs: Long): Float {
        val ageSeconds = (nowMs - location.time).coerceAtLeast(0L) / 1_000f
        val accuracyPenalty = location.accuracy
        val providerPenalty = when (location.provider) {
            LocationManager.GPS_PROVIDER -> 0f
            LocationManager.PASSIVE_PROVIDER -> 20f
            LocationManager.NETWORK_PROVIDER -> 50f
            else -> 80f
        }
        return accuracyPenalty + ageSeconds * AGE_PENALTY_PER_SECOND + providerPenalty
    }

    private fun Location.toSessionGpsPoint(): SessionGpsPoint {
        return SessionGpsPoint(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = altitude.takeIf { hasAltitude() } ?: 0.0,
            speedMps = speed.takeIf { hasSpeed() },
            bearingDegrees = bearing.takeIf { hasBearing() },
            horizontalAccuracyMeters = accuracy,
            provider = provider,
            fixTimeMillis = time,
        )
    }

    private fun isFreshCachedPoint(point: SessionGpsPoint, nowMs: Long): Boolean {
        val fixTimeMillis = point.fixTimeMillis ?: return false
        return (nowMs - fixTimeMillis).coerceAtLeast(0L) <= MAX_LOCATION_AGE_MS
    }

    private companion object {
        private const val DEFAULT_ACTIVE_REQUEST_MIN_INTERVAL_MS = 1_000L
        private const val MAX_LOCATION_AGE_MS = 15_000L
        private const val MAX_ACCEPTABLE_ACCURACY_METERS = 100f
        private const val AGE_PENALTY_PER_SECOND = 3f

        private object DirectExecutor : Executor {
            override fun execute(command: Runnable) = command.run()
        }
    }
}
