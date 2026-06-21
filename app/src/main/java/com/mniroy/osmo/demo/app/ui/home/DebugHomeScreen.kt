package com.mniroy.osmo.demo.app.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mniroy.osmo.demo.app.ui.wear.WearAppTheme
import com.mniroy.osmo.demo.app.ui.wear.WearMainScreen

@Composable
fun DebugHomeScreen(
    viewModel: DebugHomeViewModel,
    darkThemeEnabled: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
) {
    val state = viewModel.state.collectAsState().value

    WearAppTheme {
        WearMainScreen(
            state = state,
            onToggleRecord = viewModel::performWorkbenchPrimaryAction,
            onQsClick = viewModel::sendQsKeyClick,
            onSwitchMode = viewModel::switchMode,
            onSetGpsAutoPushEnabled = viewModel::setGpsAutoPushEnabled,
            onSetGpsAutoPushFrequencyHz = viewModel::setGpsAutoPushFrequencyHz,
            onSleep = viewModel::sleep,
            onWake = viewModel::wake,
            onRequestVersion = viewModel::requestVersion,
            onStartScan = viewModel::startScan,
            onStopScan = viewModel::stopScan,
            onConnect = viewModel::connect,
            onDisconnect = viewModel::disconnect,
            onPermissionAction = viewModel::performPermissionAction,
        )
    }
}
