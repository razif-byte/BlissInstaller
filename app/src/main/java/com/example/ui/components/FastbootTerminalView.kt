package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FastbootTerminalView(
    logs: List<String>,
    isFlashing: Boolean,
    progress: Float,
    isComplete: Boolean,
    currentStepTitle: String,
    onNextStepClick: () -> Unit,
    onAutoFlashClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fastboot_terminal_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TerminalBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalCyan.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Terminal Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "fastboot@redmi9t:~$",
                        color = TerminalCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isFlashing) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = TerminalCyan,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Flashing...",
                            color = TerminalYellow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else if (isComplete) {
                    Text(
                        text = "SUKSES ✓",
                        color = TerminalGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Flashing Progress Indicator
            if (isFlashing || progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TerminalCyan,
                    trackColor = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Terminal Screen Output
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF030712), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { logLine ->
                        val textColor = when {
                            logLine.contains("OKAY", ignoreCase = true) || logLine.contains("SELESAI", ignoreCase = true) || logLine.contains("SUKSES", ignoreCase = true) -> TerminalGreen
                            logLine.contains("AMARAN", ignoreCase = true) || logLine.contains("WARNING", ignoreCase = true) -> TerminalYellow
                            logLine.contains("RALAT", ignoreCase = true) || logLine.contains("ERROR", ignoreCase = true) || logLine.contains("FAILED", ignoreCase = true) -> TerminalRed
                            logLine.startsWith(">") || logLine.startsWith("==") -> TerminalCyan
                            else -> Color(0xFFCBD5E1)
                        }

                        Text(
                            text = logLine,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNextStepClick,
                    enabled = !isFlashing && !isComplete,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_next_step_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TerminalCyan
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Langkah Manual", fontSize = 12.sp, maxLines = 1)
                }

                Button(
                    onClick = onAutoFlashClick,
                    enabled = !isFlashing && !isComplete,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminal_auto_flash_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlissPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Automasi Penuh", fontSize = 12.sp, maxLines = 1)
                }
            }
        }
    }
}
