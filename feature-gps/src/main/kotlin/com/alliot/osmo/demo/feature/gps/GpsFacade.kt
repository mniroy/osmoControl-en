package com.alliot.osmo.demo.feature.gps

import com.alliot.osmo.demo.session.SessionController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GpsFacade(
    private val sessionController: SessionController,
) {
    val state: Flow<GpsState> = sessionController.status.map { status ->
        GpsState(
            active = status.gpsPushActive,
            lastResult = if (status.gpsPushActive) "GPS push active" else "GPS idle",
        )
    }

    suspend fun pushSampleGps() {
        sessionController.pushGps(
            latitude = 31.2304,
            longitude = 121.4737,
            altitudeMeters = 15.0,
        )
    }
}
