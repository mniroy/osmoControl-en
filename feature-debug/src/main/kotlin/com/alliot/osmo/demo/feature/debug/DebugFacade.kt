package com.alliot.osmo.demo.feature.debug

import com.alliot.osmo.demo.session.SessionController
import com.alliot.osmo.demo.session.log.SessionLogEntry
import kotlinx.coroutines.flow.StateFlow

class DebugFacade(
    private val sessionController: SessionController,
) {
    val logs: StateFlow<List<SessionLogEntry>> = sessionController.logs

    suspend fun sendManualCommand(hex: String) = sessionController.sendManualCommand(hex)
}
