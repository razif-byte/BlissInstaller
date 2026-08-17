package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlatformMode
import com.example.ui.components.DeviceDiagnosticCard
import com.example.ui.components.MonthlyAnalyticsChart
import com.example.ui.components.WatermarkFooter
import com.example.ui.viewmodel.BlissUiState

@Composable
fun HomeScreen(
    uiState: BlissUiState,
    onPlatformModeChange: (PlatformMode) -> Unit,
    onReScanDevice: () -> Unit,
    onNavigateToFlashing: () -> Unit,
    onNavigateToRoms: () -> Unit,
    onNavigateToWindowsSuite: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onTriggerBackup: () -> Unit,
    onCloudSync: () -> Unit,
    onUnlockWarningClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
            .testTag("home_screen_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Alert Banner (if any)
        uiState.statusBanner?.let { banner ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = banner,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Automatic Device Detection Card
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            DeviceDiagnosticCard(
                specs = uiState.deviceSpecs,
                platformMode = uiState.platformMode,
                isDetecting = uiState.isAutoDetecting,
                onPlatformModeChange = onPlatformModeChange,
                onReScan = onReScanDevice,
                onUnlockWarningClick = onUnlockWarningClick
            )
        }

        // Quick Actions Grid (Backup, Cloud Sync, ROMs, Guide)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Tindakan Pantas & Keselamatan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Sandaran Automatik",
                    subtitle = if (uiState.isBackupRunning) "Sedang menyimpan..." else "EFS, Persist & Boot",
                    icon = Icons.Default.CloudUpload,
                    color = Color(0xFF00B4D8),
                    isLoading = uiState.isBackupRunning,
                    onClick = onTriggerBackup,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = "Segerak Awan",
                    subtitle = uiState.lastCloudSyncTime,
                    icon = Icons.Default.Sync,
                    color = Color(0xFF10B981),
                    isLoading = uiState.isCloudSyncing,
                    onClick = onCloudSync,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Katalog ROM Bliss OS",
                    subtitle = "${uiState.availableRoms.size} binaan rasmi",
                    icon = Icons.Default.DownloadForOffline,
                    color = Color(0xFF8B5CF6),
                    onClick = onNavigateToRoms,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = if (uiState.platformMode == PlatformMode.ANDROID_DEVICE) "Panduan Fastboot OTG" else "Alat PC & Skrip .bat",
                    subtitle = "Langkah demi langkah",
                    icon = if (uiState.platformMode == PlatformMode.ANDROID_DEVICE) Icons.Default.Terminal else Icons.Default.LaptopWindows,
                    color = Color(0xFFF59E0B),
                    onClick = if (uiState.platformMode == PlatformMode.ANDROID_DEVICE) onNavigateToFlashing else onNavigateToWindowsSuite,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Active Selected ROM Banner
        uiState.selectedRom?.let { rom ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onNavigateToRoms() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Android,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = rom.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (rom.isRecommended) {
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DISYORKAN",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF10B981)
                                            )
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Saiz: ${rom.fileSize} • ${rom.androidVersion}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Pilih ROM",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Mini Analytics Chart
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToAnalytics() }
        ) {
            MonthlyAnalyticsChart(data = uiState.monthlyAnalytics)
        }

        // Watermark Footer
        WatermarkFooter(modifier = Modifier.padding(horizontal = 4.dp))
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = !isLoading, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = color,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
