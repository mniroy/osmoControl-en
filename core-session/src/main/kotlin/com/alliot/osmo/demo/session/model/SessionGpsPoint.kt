package com.alliot.osmo.demo.session.model

data class SessionGpsPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val speedMps: Float? = null,
    val bearingDegrees: Float? = null,
    val horizontalAccuracyMeters: Float? = null,
    val provider: String? = null,
    val fixTimeMillis: Long? = null,
)
