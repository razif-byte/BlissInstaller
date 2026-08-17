package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BootloaderStatus
import com.example.data.model.DeviceSpecs
import com.example.data.model.PlatformMode

@Composable
fun DeviceDiagnosticCard(
    specs: DeviceSpecs,
    platformMode: PlatformMode,
    isDetecting: Boolean,
    onPlatformModeChange: (PlatformMode) -> Unit,
    onReScan: () -> Unit,
    onUnlockWarningClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_diagnostic_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Auto-Detect Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (platformMode == PlatformMode.ANDROID_DEVICE) Icons.Default.PhoneAndroid else Icons.Default.LaptopWindows,
                            contentDescription = "Device Type",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "Pengesanan Peranti Automatik",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isDetecting) "Sedang mengimbas perkakasan..." else specs.modelName,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onReScan,
                    enabled = !isDetecting,
                    modifier = Modifier.testTag("rescan_device_button")
                ) {
                    if (isDetecting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Imbas Semula")
                    }
                }
            }

            // Platform Mode Toggle Tabs (Windows vs Android)
            TabRow(
                selectedTabIndex = if (platformMode == PlatformMode.ANDROID_DEVICE) 0 else 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = platformMode == PlatformMode.ANDROID_DEVICE,
                    onClick = { onPlatformModeChange(PlatformMode.ANDROID_DEVICE) },
                    modifier = Modifier.testTag("tab_android_mode"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Mod Android (MicroSD)")
                        }
                    }
                )
                Tab(
                    selected = platformMode == PlatformMode.WINDOWS_PC,
                    onClick = { onPlatformModeChange(PlatformMode.WINDOWS_PC) },
                    modifier = Modifier.testTag("tab_windows_mode"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.LaptopWindows, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Mod Windows (PC)")
                        }
                    }
                )
            }

            // Diagnostic Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge(
                    label = "Codename",
                    value = specs.codename,
                    icon = Icons.Default.Memory,
                    modifier = Modifier.weight(1f)
                )
                SpecBadge(
                    label = "Cipset",
                    value = "Snapdragon 662",
                    icon = Icons.Default.DeveloperBoard,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge(
                    label = "Bateri",
                    value = "${specs.batteryLevel}% (Selamat >60%)",
                    icon = Icons.Default.BatteryChargingFull,
                    color = if (specs.batteryLevel >= 60) Color(0xFF10B981) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                SpecBadge(
                    label = "Bootloader",
                    value = specs.bootloaderStatus.name,
                    icon = if (specs.bootloaderStatus == BootloaderStatus.UNLOCKED) Icons.Default.LockOpen else Icons.Default.Lock,
                    color = if (specs.bootloaderStatus == BootloaderStatus.UNLOCKED) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = onUnlockWarningClick
                )
            }

            // Fastboot Safety Warning Banner
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Perhatian: Flashing ROM akan memadamkan data peranti. Buat sandaran automatik sebelum meneruskan.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = if (onClick != null) modifier.clip(RoundedCornerShape(10.dp)) else modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = color,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
