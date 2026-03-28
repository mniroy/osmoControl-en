package com.alliot.osmo.demo.app.ui

import com.alliot.osmo.demo.app.ui.home.RealModePrerequisites
import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionPrerequisitesTest {

    @Test
    fun `missing permissions with rationale stay on request action`() {
        val prerequisites = resolveRealModePrerequisites(
            permissionStates = listOf(
                PermissionState(granted = false, requestedBefore = true, shouldShowRationale = true),
                PermissionState(granted = true, requestedBefore = true, shouldShowRationale = false),
            ),
            locationGranted = false,
            locationRequestedBefore = true,
            locationShouldShowRationale = true,
        )

        assertEquals(
            RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
                requiresSettingsAction = false,
            ),
            prerequisites,
        )
    }

    @Test
    fun `missing permissions without rationale after prior request require settings`() {
        val prerequisites = resolveRealModePrerequisites(
            permissionStates = listOf(
                PermissionState(granted = false, requestedBefore = true, shouldShowRationale = false),
                PermissionState(granted = true, requestedBefore = true, shouldShowRationale = false),
            ),
            locationGranted = false,
            locationRequestedBefore = true,
            locationShouldShowRationale = false,
        )

        assertEquals(
            RealModePrerequisites(
                bluetoothPermissionsGranted = false,
                locationPermissionGranted = false,
                requiresSettingsAction = true,
            ),
            prerequisites,
        )
    }
}
