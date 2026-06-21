package com.mniroy.osmo.demo.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mniroy.osmo.demo.app.ui.theme.SignalDanger
import com.mniroy.osmo.demo.app.ui.theme.SignalReady
import com.mniroy.osmo.demo.app.ui.theme.SignalWarn
import com.mniroy.osmo.demo.app.ui.theme.ActionInk
import com.mniroy.osmo.demo.session.model.HandshakeStage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WorkbenchScreen(
    state: DebugHomeState,
    listState: LazyListState,
    isLandscape: Boolean,
    onToggleRecord: () -> Unit,
    onOpenModeSheet: () -> Unit,
    onOpenGpsSheet: () -> Unit,
    onOpenDeviceActionsSheet: () -> Unit,
    onPermissionAction: () -> Unit,
    onOpenRecentEvents: () -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        StatusOverviewCard(state = state)
                    }
                    Column(
                        modifier = Modifier.weight(1.08f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PrimaryActionCard(state = state, onToggleRecord = onToggleRecord)
                        QuickActionRow(
                            state = state,
                            onOpenModeSheet = onOpenModeSheet,
                            onOpenGpsSheet = onOpenGpsSheet,
                            onOpenDeviceActionsSheet = onOpenDeviceActionsSheet,
                        )
                        RecentEventsCard(state = state, onOpenRecentEvents = onOpenRecentEvents)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    PrimaryActionCard(state = state, onToggleRecord = onToggleRecord)
                    QuickActionRow(
                        state = state,
                        onOpenModeSheet = onOpenModeSheet,
                        onOpenGpsSheet = onOpenGpsSheet,
                        onOpenDeviceActionsSheet = onOpenDeviceActionsSheet,
                    )
                    StatusOverviewCard(state = state)
                    RecentEventsCard(state = state, onOpenRecentEvents = onOpenRecentEvents)
                }
            }
        }
    }
}

@Composable
private fun PrimaryActionCard(
    state: DebugHomeState,
    onToggleRecord: () -> Unit,
) {
    val snapshot = state.cameraStatus
    val workbench = state.workbenchUiModel
    val photoMode = isPhotoCaptureMode(snapshot)
    val modeTitle = snapshot.modeName.ifBlank { snapshot.modeLabel }
    val modeSubtitle = snapshot.modeParameters.ifBlank {
        buildString {
            append(resolutionLabel(snapshot.videoResolution))
            append(" / ")
            append("${fpsLabel(snapshot.fpsIndex)}fps")
            append(" / ")
            append(if (state.sessionStatus.gpsPushActive) "GPS Sync" else "GPS Standby")
        }
    }
    val buttonLabel = primaryActionButtonLabel(snapshot, workbench.recordActionEnabled)
    val statusText = primaryActionStatusText(state)
    val helperText = primaryActionHelperText(state)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (snapshot.recording) SignalDanger else MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = modeTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = modeSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RecordToggleButton(
                        recording = snapshot.recording && !photoMode,
                        enabled = workbench.recordActionEnabled,
                        contentDescription = primaryActionContentDescription(snapshot),
                        onClick = onToggleRecord,
                    )
                    Text(
                        text = buttonLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (workbench.recordActionEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = if (snapshot.recording || workbench.recordActionDisabledReason != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun RecordToggleButton(
    recording: Boolean,
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val accentColor = when {
        !enabled -> MaterialTheme.colorScheme.outline
        recording -> SignalDanger
        else -> SignalReady
    }
    val ringFill = accentColor.copy(alpha = if (enabled) 0.16f else 0.08f)
    val ringStroke = accentColor.copy(alpha = if (enabled) 0.45f else 0.32f)
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(ringFill, CircleShape)
            .border(width = 1.5.dp, color = ringStroke, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        FilledIconButton(
            modifier = Modifier
                .size(50.dp)
                .semantics { this.contentDescription = contentDescription },
            onClick = rememberHapticClick(
                kind = if (recording) HomeHapticKind.DANGER else HomeHapticKind.PRIMARY,
                onClick = onClick,
            ),
            enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = accentColor,
                contentColor = ActionInk,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        ) {
            RecordToggleGlyph(recording = recording)
        }
        if (!enabled) {
            DisabledRecordOverlay(strokeColor = ringStroke)
        }
    }
}

@Composable
private fun RecordToggleGlyph(recording: Boolean) {
    val tint = LocalContentColor.current
    val shape = if (recording) RoundedCornerShape(4.dp) else CircleShape
    val size = if (recording) 16.dp else 18.dp
    Box(
        modifier = Modifier
            .size(size)
            .background(color = tint, shape = shape),
    )
}

@Composable
private fun DisabledRecordOverlay(strokeColor: Color) {
    Box(
        modifier = Modifier
            .size(width = 28.dp, height = 4.dp)
            .rotate(-35f)
            .background(strokeColor, RoundedCornerShape(999.dp)),
    )
}

@Composable
private fun QuickActionRow(
    state: DebugHomeState,
    onOpenModeSheet: () -> Unit,
    onOpenGpsSheet: () -> Unit,
    onOpenDeviceActionsSheet: () -> Unit,
) {
    val workbench = state.workbenchUiModel
    if (!workbench.showModeQuickAction && !workbench.showGpsQuickAction && !workbench.showDeviceActionsQuickAction) {
        return
    }
    val session = state.sessionStatus
    val modeName = state.cameraStatus.modeName.ifBlank { state.cameraStatus.modeLabel.ifBlank { "Current Mode" } }
    val gpsAccent = when {
        session.gpsAutoPushEnabled -> SignalReady
        state.permissionCta != null -> MaterialTheme.colorScheme.error
        else -> SignalWarn
    }
    val controlReady =
        session.connectedDevice != null &&
        session.protocolReady &&
        session.handshakeStage == HandshakeStage.COMPLETED
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (workbench.showModeQuickAction) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                accent = MaterialTheme.colorScheme.primary,
                kind = QuickActionKind.MODE,
                title = "Mode",
                status = when {
                    !workbench.modeSwitchEnabled -> "Locked"
                    else -> modeName
                },
                subtitle = workbench.modeSwitchBlockedReason ?: "${workbench.modeOptions.size} modes available",
                actionLabel = "Switch",
                enabled = true,
                onClick = onOpenModeSheet,
            )
        }
        if (workbench.showGpsQuickAction) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                accent = gpsAccent,
                kind = QuickActionKind.GPS,
                title = "GPS",
                status = if (session.gpsAutoPushEnabled) {
                    "${session.gpsAutoPushHz}Hz"
                } else if (state.permissionCta != null) {
                    "Pending Authorization"
                } else {
                    "Standby"
                },
                subtitle = if (session.gpsAutoPushEnabled) {
                    session.lastGpsCoordinate ?: (session.lastGpsResult ?: "Auto Syncing")
                } else {
                    workbench.gpsActionDisabledReason ?: "Auto Sync Disabled"
                },
                actionLabel = "Configure",
                enabled = true,
                onClick = onOpenGpsSheet,
            )
        }
        if (workbench.showDeviceActionsQuickAction) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                accent = if (state.busyAction != null) SignalDanger else SignalWarn,
                kind = QuickActionKind.DEVICE,
                title = "Device Actions",
                status = when {
                    state.busyAction != null -> "Processing"
                    controlReady -> "Ready"
                    session.connectedDevice != null -> "Connected"
                    else -> "Offline"
                },
                subtitle = state.busyAction ?: if (controlReady) {
                    "Sleep / Wake / Version"
                } else {
                    "Available after device connected"
                },
                actionLabel = "Open",
                enabled = true,
                onClick = onOpenDeviceActionsSheet,
            )
        }
    }
}

private enum class QuickActionKind {
    MODE,
    GPS,
    DEVICE,
}

private const val QUICK_ACTION_SUBTITLE_LINES = 2

@Composable
private fun QuickActionButton(
    accent: Color,
    kind: QuickActionKind,
    title: String,
    status: String,
    subtitle: String,
    actionLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val baseColor = MaterialTheme.colorScheme.surface
    val borderColor = if (enabled) accent.copy(alpha = 0.32f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.36f)
    val overlay = if (enabled) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
    Card(
        modifier = modifier
            .heightIn(min = 112.dp)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = rememberHapticClick(
                    kind = HomeHapticKind.SECONDARY,
                    onClick = onClick,
                ),
            ),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(overlay, Color.Transparent),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    QuickActionGlyph(kind = kind, accent = accent, enabled = enabled)
                    QuickActionStatusPill(
                        label = status,
                        accent = accent,
                        enabled = enabled,
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        minLines = QUICK_ACTION_SUBTITLE_LINES,
                        maxLines = QUICK_ACTION_SUBTITLE_LINES,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    QuickActionChevron(accent = accent, enabled = enabled)
                }
            }
        }
    }
}

@Composable
private fun QuickActionStatusPill(
    label: String,
    accent: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (enabled) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickActionGlyph(
    kind: QuickActionKind,
    accent: Color,
    enabled: Boolean,
) {
    val tint = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(
                color = tint.copy(alpha = if (enabled) 0.12f else 0.08f),
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (kind) {
            QuickActionKind.MODE -> ModeGlyph(tint = tint)
            QuickActionKind.GPS -> GpsGlyph(tint = tint)
            QuickActionKind.DEVICE -> DeviceGlyph(tint = tint)
        }
    }
}

@Composable
private fun ModeGlyph(tint: Color) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .size(width = 16.dp, height = 4.dp)
                    .background(tint, RoundedCornerShape(999.dp)),
            )
        }
    }
}

@Composable
private fun GpsGlyph(tint: Color) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(1.5.dp, tint, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(tint, CircleShape),
        )
    }
}

@Composable
private fun DeviceGlyph(tint: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = if (index == 1) 16.dp else 10.dp)
                    .background(tint, RoundedCornerShape(999.dp)),
            )
        }
    }
}

@Composable
private fun QuickActionChevron(
    accent: Color,
    enabled: Boolean,
) {
    val tint = if (enabled) accent else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChevronBar(tint = tint, long = false)
        ChevronBar(tint = tint, long = true)
    }
}

@Composable
private fun RowScope.ChevronBar(
    tint: Color,
    long: Boolean,
) {
    Box(
        modifier = Modifier
            .size(width = if (long) 8.dp else 5.dp, height = 2.dp)
            .background(tint, RoundedCornerShape(999.dp)),
    )
}

@Composable
private fun StatusOverviewCard(state: DebugHomeState) {
    HomeSectionCard(title = "Status Overview") {
        state.workbenchUiModel.statusOverviewItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { item ->
                    SummaryPill(
                        modifier = Modifier.weight(1f),
                        title = item.title,
                        value = item.value,
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RecentEventsCard(
    state: DebugHomeState,
    onOpenRecentEvents: () -> Unit,
) {
    val onCardClick = rememberHapticClick(
        kind = HomeHapticKind.NAVIGATION,
        onClick = onOpenRecentEvents,
    )
    HomeSectionCard(
        title = "Recent Events",
        modifier = Modifier.clickable(
            enabled = state.workbenchUiModel.recentEvents.isNotEmpty(),
            onClick = onCardClick,
        ),
    ) {
        if (state.workbenchUiModel.recentEvents.isEmpty()) {
            Text(
                text = "Waiting for device status or error events.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.workbenchUiModel.recentEvents.forEach { event ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = event.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = formatEventTime(event.timestampMillis),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
            Text(
                text = "Click to view debug console log",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val EVENT_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

private fun formatEventTime(timestampMillis: Long): String {
    return EVENT_TIME_FORMATTER.format(Instant.ofEpochMilli(timestampMillis))
}
