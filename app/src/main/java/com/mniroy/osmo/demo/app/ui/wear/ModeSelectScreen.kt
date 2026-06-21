package com.mniroy.osmo.demo.app.ui.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.mniroy.osmo.demo.app.ui.home.DebugHomeState
import com.mniroy.osmo.demo.app.ui.home.workbenchModeLabel

/**
 * Wear OS mode-select screen.
 * Shows available camera modes in a ScalingLazyColumn.
 * Tapping a mode triggers the switch if control is ready.
 */
@Composable
fun ModeSelectScreen(
    state: DebugHomeState,
    onSwitchMode: (Int) -> Unit,
) {
    val modeOptions = state.workbenchUiModel.modeOptions
    val currentMode = state.cameraStatus.mode
    val switchEnabled = state.workbenchUiModel.modeSwitchEnabled
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Text(
                    text = "MODE",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = LimeAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (modeOptions.isEmpty()) {
                item {
                    Text(
                        text = "No modes\navailable",
                        fontSize = 12.sp,
                        color = DimText,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                items(modeOptions) { option ->
                    val isActive = option.mode == currentMode
                    val bgColor = when {
                        isActive -> SpeedAccent.copy(alpha = 0.22f)
                        else -> Color(0xFF0D1117)
                    }
                    val textColor = when {
                        isActive -> SpeedAccent
                        switchEnabled -> Color(0xFFECEFF1)
                        else -> DimText
                    }
                    val borderColor = if (isActive) SpeedAccent.copy(alpha = 0.5f) else Color(0xFF263238)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor, RoundedCornerShape(14.dp))
                            .then(
                                if (switchEnabled && !isActive) {
                                    Modifier.clickable(
                                        role = Role.Button,
                                        onClick = { onSwitchMode(option.mode) },
                                    )
                                } else Modifier,
                            )
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = option.label,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = textColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            item {
                // Blocked reason hint
                val reason = state.workbenchUiModel.modeSwitchBlockedReason
                if (reason != null) {
                    Text(
                        text = reason,
                        fontSize = 10.sp,
                        color = GpsNoFix,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
