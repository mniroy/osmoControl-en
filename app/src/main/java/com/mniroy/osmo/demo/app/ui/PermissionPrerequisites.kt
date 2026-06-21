package com.mniroy.osmo.demo.app.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.ActivityCompat
import com.mniroy.osmo.demo.app.ui.home.RealModePrerequisites

data class PermissionState(
    val granted: Boolean,
    val requestedBefore: Boolean,
    val shouldShowRationale: Boolean,
)

fun resolveRealModePrerequisites(
    permissionStates: List<PermissionState>,
    locationGranted: Boolean,
    locationRequestedBefore: Boolean,
    locationShouldShowRationale: Boolean,
): RealModePrerequisites {
    val bluetoothPermissionsGranted = permissionStates.all { it.granted }
    val locationPermissionGranted = locationGranted
    val missingStates = permissionStates.filterNot { it.granted } + listOfNotNull(
        if (locationGranted) null else PermissionState(
            granted = false,
            requestedBefore = locationRequestedBefore,
            shouldShowRationale = locationShouldShowRationale,
        ),
    )
    val requiresSettingsAction = missingStates.any { state ->
        state.requestedBefore && !state.shouldShowRationale
    }
    return RealModePrerequisites(
        bluetoothPermissionsGranted = bluetoothPermissionsGranted,
        locationPermissionGranted = locationPermissionGranted,
        requiresSettingsAction = requiresSettingsAction,
    )
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Activity.permissionState(
    permission: String,
    granted: Boolean,
    requestedBefore: Boolean,
): PermissionState {
    return PermissionState(
        granted = granted,
        requestedBefore = requestedBefore,
        shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission),
    )
}
