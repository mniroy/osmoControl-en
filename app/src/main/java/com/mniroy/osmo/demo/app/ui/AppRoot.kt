package com.mniroy.osmo.demo.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mniroy.osmo.demo.app.di.AppContainer
import com.mniroy.osmo.demo.app.preferences.AppPreferences
import com.mniroy.osmo.demo.app.ui.home.DebugHomeScreen
import com.mniroy.osmo.demo.app.ui.home.DebugHomeAction
import com.mniroy.osmo.demo.app.ui.home.RealModePrerequisites
import com.mniroy.osmo.demo.app.ui.home.DebugHomeViewModel
import com.mniroy.osmo.demo.app.ui.home.DebugHomeViewModelFactory
import com.mniroy.osmo.demo.app.ui.theme.OsmoDemoTheme
import kotlinx.coroutines.flow.collect

private const val PERMISSION_PREFS = "app_permissions"

@Composable
fun AppRoot(container: AppContainer) {
    val context = LocalContext.current
    val systemDarkTheme = isSystemInDarkTheme()
    val lifecycleOwner = LocalLifecycleOwner.current
    val appPreferences = remember(context) { AppPreferences(context) }
    val factory = remember(container, appPreferences) { DebugHomeViewModelFactory(container, appPreferences) }
    val viewModel: DebugHomeViewModel = viewModel(factory = factory)
    val permissions = remember(context) { requiredPermissions() }
    val prefs = remember(context) { context.getSharedPreferences(PERMISSION_PREFS, Context.MODE_PRIVATE) }
    var darkThemeEnabled by remember(context, systemDarkTheme) {
        mutableStateOf(appPreferences.isDarkThemeEnabled(systemDarkTheme))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        prefs.edit().apply {
            permissions.forEach { permission -> putBoolean(permission, true) }
        }.apply()
        viewModel.updatePrerequisites(context.collectPrerequisites(prefs))
    }

    OsmoDemoTheme(darkTheme = darkThemeEnabled) {
        var showExitDialog by remember { mutableStateOf(false) }

        androidx.activity.compose.BackHandler(enabled = !showExitDialog) {
            showExitDialog = true
        }

        DebugHomeScreen(
            viewModel = viewModel,
            darkThemeEnabled = darkThemeEnabled,
            onToggleDarkTheme = { enabled ->
                darkThemeEnabled = enabled
                appPreferences.setDarkThemeEnabled(enabled)
            },
        )

        androidx.wear.compose.material.dialog.Dialog(
            showDialog = showExitDialog,
            onDismissRequest = { showExitDialog = false }
        ) {
            androidx.wear.compose.material.dialog.Alert(
                title = { 
                    androidx.wear.compose.material.Text(
                        text = "Close Remote?", 
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                    ) 
                },
                negativeButton = {
                    androidx.wear.compose.material.Button(
                        onClick = { showExitDialog = false }, 
                        colors = androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors()
                    ) {
                        androidx.wear.compose.material.Text("No")
                    }
                },
                positiveButton = {
                    androidx.wear.compose.material.Button(
                        onClick = {
                            showExitDialog = false
                            context.findActivity()?.finish()
                        }
                    ) {
                        androidx.wear.compose.material.Text("Yes")
                    }
                }
            )
        }
    }

    DisposableEffect(lifecycleOwner, context, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.updatePrerequisites(context.collectPrerequisites(prefs))
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                viewModel.disconnect(forget = false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel, context, permissions) {
        viewModel.actions.collect { action ->
            when (action) {
                DebugHomeAction.RequestPermissions -> {
                    permissionLauncher.launch(permissions.toTypedArray())
                }
                DebugHomeAction.OpenSettings -> {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    )
                    context.startActivity(intent)
                }
                DebugHomeAction.ResetAppState -> {
                    prefs.edit().clear().apply()
                    appPreferences.clear()
                    container.clearPersistentState()
                    context.findActivity()?.recreate()
                }
            }
        }
    }
}

private fun Context.collectPrerequisites(
    prefs: android.content.SharedPreferences,
): RealModePrerequisites {
    val activity = findActivity()
    val bluetoothStates = bluetoothPermissions().map { permission ->
        val granted = hasPermission(permission)
        if (activity != null) {
            activity.permissionState(
                permission = permission,
                granted = granted,
                requestedBefore = prefs.getBoolean(permission, false),
            )
        } else {
            PermissionState(
                granted = granted,
                requestedBefore = prefs.getBoolean(permission, false),
                shouldShowRationale = false,
            )
        }
    }
    val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    val locationGranted = hasPermission(locationPermission)
    val locationRequestedBefore = prefs.getBoolean(locationPermission, false)
    val locationShouldShowRationale = activity?.let { currentActivity ->
        androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, locationPermission)
    } ?: false
    return resolveRealModePrerequisites(
        permissionStates = bluetoothStates,
        locationGranted = locationGranted,
        locationRequestedBefore = locationRequestedBefore,
        locationShouldShowRationale = locationShouldShowRationale,
    )
}

private fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

private fun requiredPermissions(): List<String> = buildList {
    addAll(bluetoothPermissions())
    add(Manifest.permission.ACCESS_FINE_LOCATION)
}

private fun bluetoothPermissions(): List<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
        )
    }
}
