package com.alliot.osmo.demo.session.log

import com.alliot.osmo.demo.session.model.LogCategory

data class SessionLogEntry(
    val category: LogCategory,
    val message: String,
    val hex: String? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
)
