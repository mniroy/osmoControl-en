package com.mniroy.osmo.demo.feature.gps

data class GpsState(
    val active: Boolean,
    val latestCoordinateText: String = "31.2304, 121.4737",
    val lastResult: String = "",
)
