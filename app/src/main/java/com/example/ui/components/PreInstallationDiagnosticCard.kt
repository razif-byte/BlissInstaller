package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.AppBranding
import com.example.data.model.DeviceSpecs

@Composable
fun PreInstallationDiagnosticCard(
    specs: DeviceSpecs,
    isDiagnosing: Boolean,
    isOptimizing: Boolean,
    onRunDiagnostic: () -> Unit,
    onAutoOptimize: () -> Unit,
    onViewFullDiagnostic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBatteryGood = specs.isBatterySufficient
    val isStorageGood = specs.isStorageSufficient
    val isRamGood = specs.isRamSufficient
    val allMet = specs.allRequirementsMet

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pre_installation_diagnostic_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = BorderStroke(
            1.dp,
            if (allMet) Color(0xFF10B981).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title & Readiness Badge
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
                            .background(
                                if (allMet) Color(0xFF10B981).copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (allMet) Icons.Default.CheckCircle else Icons.Default.HealthAndSafety,
                            contentDescription = "Diagnostic Health",
                            tint = if (allMet) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pra-Pemasangan Diagnostic Tool",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Pemeriksaan Bateri & Ruang Storan Redmi 9T",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Surface(
                    color = if (allMet) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (allMet) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (allMet) "100% SEDIA" else "PERLU OPTIMUM",
                        color = if (allMet) Color(0xFF10B981) else Color(0xFFF59E0B),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Key Live Gauges: Battery and Storage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Real Battery Metric Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(
                        1.dp,
                        if (isBatteryGood) Color(0xFF10B981).copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (specs.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                    contentDescription = "Battery",
                                    tint = if (isBatteryGood) Color(0xFF10B981) else Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Aras Bateri",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Text(
                                text = "${specs.batteryLevel}%",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBatteryGood) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { specs.batteryLevel / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isBatteryGood) Color(0xFF10B981) else Color(0xFFEF4444),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = if (specs.isCharging) "⚡ ${specs.powerSource}" else "⚠️ Minima 60% diperlukan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = if (specs.isCharging) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }

                // 2. Real Storage Space Metric Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(
                        1.dp,
                        if (isStorageGood) Color(0xFF10B981).copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Storage",
                                    tint = if (isStorageGood) MaterialTheme.colorScheme.primary else Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Storan Bebas",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Text(
                                text = specs.freeStorageFormatted,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStorageGood) MaterialTheme.colorScheme.primary else Color(0xFFEF4444)
                                )
                            )
                        }

                        LinearProgressIndicator(
                            progress = { specs.storageUsagePercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Text(
                            text = "Jumlah: ${specs.totalStorageFormatted} (Min 6GB)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }
                }
            }

            // Secondary Hardware Badges (RAM, Temp, Bootloader)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatusBadge(
                    label = "RAM Bebas",
                    value = specs.freeRamFormatted,
                    icon = Icons.Default.Memory,
                    isPassed = isRamGood,
                    modifier = Modifier.weight(1f)
                )
                MiniStatusBadge(
                    label = "Suhu Terma",
                    value = "${specs.batteryTemperature.toInt()}°C",
                    icon = Icons.Default.Thermostat,
                    isPassed = specs.batteryTemperature <= 42f,
                    modifier = Modifier.weight(1f)
                )
                MiniStatusBadge(
                    label = "Cache App",
                    value = specs.appCacheSizeFormatted,
                    icon = Icons.Default.CleaningServices,
                    isPassed = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Auto-Optimization & Full Diagnostic Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Auto Optimize Button
                Button(
                    onClick = onAutoOptimize,
                    enabled = !isOptimizing,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("auto_optimize_app_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allMet) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                    )
                ) {
                    if (isOptimizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mengoptimumkan...", fontSize = 12.sp)
                    } else {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (allMet) "⚡ Auto Optimize App" else "⚡ Auto Optimize Sekarang",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Full Diagnostic View Button
                OutlinedButton(
                    onClick = onViewFullDiagnostic,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_full_diagnostic_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Perincian", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun MiniStatusBadge(
    label: String,
    value: String,
    icon: ImageVector,
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPassed) MaterialTheme.colorScheme.primary else Color(0xFFEF4444),
                modifier = Modifier.size(15.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
