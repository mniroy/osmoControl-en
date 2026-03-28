package com.alliot.osmo.demo.app.di

import android.content.Context
import com.alliot.osmo.demo.app.ble.AndroidBleClient
import com.alliot.osmo.demo.app.gps.AndroidGpsPointProvider
import com.alliot.osmo.demo.app.identity.ControllerIdentityStore
import com.alliot.osmo.demo.app.identity.PairedCameraStore
import com.alliot.osmo.demo.feature.control.ControlFacade
import com.alliot.osmo.demo.feature.gps.GpsFacade
import com.alliot.osmo.demo.session.SessionController
import com.alliot.osmo.demo.session.fake.FakeSessionController
import com.alliot.osmo.demo.session.real.BleSessionController

class AppContainer(
    context: Context,
) {
    private val bleClient = AndroidBleClient(context)
    private val gpsPointProvider = AndroidGpsPointProvider(context)
    private val controllerIdentityStore = ControllerIdentityStore(context)
    private val pairedCameraStore = PairedCameraStore(context)

    val fakeController: SessionController = FakeSessionController()
    val realController: SessionController = BleSessionController(
        bleClient = bleClient,
        controllerDeviceId = controllerIdentityStore.controllerDeviceId(),
        fallbackControllerMac = controllerIdentityStore.controllerMacAddressBytes(),
        verifyCodeProvider = controllerIdentityStore::nextVerifyCode,
        isKnownPairedDevice = pairedCameraStore::isPaired,
        onDevicePaired = pairedCameraStore::markPaired,
        gpsPointProvider = gpsPointProvider::latestPoint,
        gpsRequestIntervalUpdater = gpsPointProvider::setActiveRequestIntervalMs,
    )

    fun controlFacade(controller: SessionController): ControlFacade = ControlFacade(controller)
    fun gpsFacade(controller: SessionController): GpsFacade = GpsFacade(controller)

    fun clearPersistentState() {
        controllerIdentityStore.clear()
        pairedCameraStore.clear()
    }
}
