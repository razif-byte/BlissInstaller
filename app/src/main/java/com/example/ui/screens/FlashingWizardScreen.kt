package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DangerLevel
import com.example.data.model.FastbootStep
import com.example.ui.components.FastbootTerminalView
import com.example.ui.components.WatermarkFooter
import com.example.ui.viewmodel.BlissUiState

@Composable
fun FlashingWizardScreen(
    uiState: BlissUiState,
    onExecuteNextStep: () -> Unit,
    onExecuteAutomated: () -> Unit,
    onTriggerBackup: () -> Unit,
    onAutoOptimize: () -> Unit,
    onViewFullDiagnostic: () -> Unit,
    onOpenExportReport: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("flashing_wizard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pre-Installation Diagnostic Status Bar (Battery & Storage Check)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wizard_pre_install_gate"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.deviceSpecs.allRequirementsMet)
                        Color(0xFF10B981).copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (uiState.deviceSpecs.allRequirementsMet) Color(0xFF10B981).copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.deviceSpecs.allRequirementsMet) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (uiState.deviceSpecs.allRequirementsMet) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (uiState.deviceSpecs.allRequirementsMet) "Status Pra-Pemasangan: Sedia" else "Pemeriksaan Pra-Pemasangan Diperlukan",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        TextButton(onClick = onViewFullDiagnostic) {
                            Text("Perincian", fontSize = 11.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔋 Bateri: ${uiState.deviceSpecs.batteryLevel}% (${if (uiState.deviceSpecs.isBatterySufficient) "OK" else "<60%!"})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.deviceSpecs.isBatterySufficient) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        )
                        Text(
                            text = "💾 Storan: ${uiState.deviceSpecs.freeStorageFormatted} (Min 6GB)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.deviceSpecs.isStorageSufficient) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        )
                        Text(
                            text = "⚡ RAM: ${uiState.deviceSpecs.freeRamFormatted}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (!uiState.deviceSpecs.allRequirementsMet || uiState.autoOptimizeEcoModeActive) {
                        Button(
                            onClick = onAutoOptimize,
                            enabled = !uiState.isOptimizing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wizard_auto_optimize_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.deviceSpecs.allRequirementsMet) MaterialTheme.colorScheme.primary else Color(0xFF10B981)
                            )
                        ) {
                            if (uiState.isOptimizing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sedang mengoptimumkan ruang & memori...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (uiState.deviceSpecs.allRequirementsMet) "⚡ Jalankan Auto-Optimize Tambahan" else "⚡ Jalankan Auto-Optimize Sekarang (Padam Cache & Free RAM)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Fastboot Live Terminal View
        item {
            FastbootTerminalView(
                logs = uiState.terminalLogs,
                isFlashing = uiState.isFlashingActive,
                progress = uiState.flashProgress,
                isComplete = uiState.isFlashComplete,
                currentStepTitle = uiState.fastbootSteps.getOrNull(uiState.currentStepIndex)?.title ?: "Selesai",
                onNextStepClick = onExecuteNextStep,
                onAutoFlashClick = onExecuteAutomated
            )
        }

        // Backup Card status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00B4D8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF00B4D8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Status Sandaran EFS & Partisi",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = uiState.backupStatusMessage,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = onTriggerBackup,
                        enabled = !uiState.isBackupRunning,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("wizard_trigger_backup_btn")
                    ) {
                        Text(if (uiState.isBackupRunning) "Menyimpan..." else "Backup", fontSize = 12.sp)
                    }
                }
            }
        }

        // Steps Header & Export Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Langkah-Langkah Fastboot:",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                FilledTonalButton(
                    onClick = onOpenExportReport,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_report_button")
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jana Laporan", fontSize = 12.sp)
                }
            }
        }

        // Fastboot Steps List
        itemsIndexed(uiState.fastbootSteps) { index, step ->
            val isCurrent = index == uiState.currentStepIndex
            val isPassed = step.isExecuted

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_card_$index"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isPassed -> Color(0xFF10B981).copy(alpha = 0.12f)
                        isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    }
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isCurrent) 2.dp else 1.dp,
                    when {
                        isPassed -> Color(0xFF10B981)
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPassed -> Color(0xFF10B981)
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPassed) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Danger Tag
                        val dangerColor = when (step.dangerLevel) {
                            DangerLevel.LOW -> Color(0xFF10B981)
                            DangerLevel.MEDIUM -> Color(0xFF00B4D8)
                            DangerLevel.HIGH -> Color(0xFFF59E0B)
                            DangerLevel.CRITICAL -> Color(0xFFEF4444)
                        }

                        Surface(
                            color = dangerColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = step.dangerLevel.name,
                                color = dangerColor,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    // Command Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070B12))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = step.command,
                            color = Color(0xFF4ADE80),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    if (step.outputLog.isNotBlank()) {
                        Text(
                            text = "Log: ${step.outputLog}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF10B981),
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        }

        // Unbrick & Emergency Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = "Kecemasan: Pemulihan Bootloop & Anti-Brick",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                    Text(
                        text = "Jika telefon tersekat pada logo Redmi, tekan Volume Down + Power selama 10 saat untuk masuk semula ke mod Fastboot, kemudian klik 'Automasi Penuh' atau gunakan alat Windows.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        }

        item {
            WatermarkFooter()
        }
    }
}
