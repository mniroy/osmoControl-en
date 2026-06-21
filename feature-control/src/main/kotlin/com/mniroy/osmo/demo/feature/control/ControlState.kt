package com.mniroy.osmo.demo.feature.control

data class ControlState(
    val canSendCommands: Boolean,
    val recording: Boolean,
    val currentMode: Int,
    val lastResult: String = "",
)
