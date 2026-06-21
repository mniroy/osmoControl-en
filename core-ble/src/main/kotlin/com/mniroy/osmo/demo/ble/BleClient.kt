package com.mniroy.osmo.demo.ble

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BleClient {
    val scanResults: StateFlow<List<BleScanResult>>
    val connectionState: StateFlow<BleConnectionState>
    val events: Flow<BleEvent>

    suspend fun startScan()
    suspend fun stopScan()
    suspend fun connect(macAddress: String)
    suspend fun disconnect()
    suspend fun write(bytes: ByteArray)
    suspend fun startWakeAdvertising(reversedMac: ByteArray)
}
