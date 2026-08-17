package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppBranding
import com.example.data.model.BlissRomVersion
import com.example.data.model.RomVariant
import com.example.ui.components.WatermarkFooter
import com.example.ui.viewmodel.BlissUiState

@Composable
fun RomManagerScreen(
    uiState: BlissUiState,
    onSelectRom: (BlissRomVersion) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: ROMs, 1: MicroSD Prep, 2: Checksum Validator

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rom_manager_screen")
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Katalog ROM") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Penyediaan MicroSD") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Pengesah SHA256") }
            )
        }

        when (selectedTab) {
            0 -> RomCatalogTab(
                roms = uiState.availableRoms,
                selectedRom = uiState.selectedRom,
                onSelectRom = onSelectRom
            )
            1 -> MicroSdPrepTab()
            2 -> ChecksumValidatorTab(selectedRom = uiState.selectedRom)
        }
    }
}

@Composable
private fun RomCatalogTab(
    roms: List<BlissRomVersion>,
    selectedRom: BlissRomVersion?,
    onSelectRom: (BlissRomVersion) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Official Bliss OS Docs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dokumentasi Rasmi Bliss OS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Text(
                            text = "Panduan rasmi binaan ARM64 Snapdragon 662 untuk Redmi 9T (Chime)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppBranding.BLISS_DOCS_URL))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buka Docs")
                    }
                }
            }
        }

        item {
            Text(
                text = "Pilih Versi Bliss OS:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(roms) { rom ->
            val isSelected = selectedRom?.id == rom.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRom(rom) }
                    .testTag("rom_item_${rom.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = rom.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                if (rom.isRecommended) {
                                    Surface(
                                        color = Color(0xFF10B981),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "RECOMMENDED",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${rom.version} • Tarikh: ${rom.buildDate}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectRom(rom) }
                        )
                    }

                    // Metadata chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text("Saiz: ${rom.fileSize}") },
                            leadingIcon = { Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(rom.variant.name) },
                            leadingIcon = { Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    // Changelog summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sorotan Perubahan (Changelog):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        rom.changelog.take(3).forEach { change ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("•", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary))
                                Text(change, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                            }
                        }
                    }

                    // Download Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rom.downloadUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Muat Turun Fail ROM")
                        }
                    }
                }
            }
        }

        item {
            WatermarkFooter()
        }
    }
}

@Composable
private fun MicroSdPrepTab() {
    val context = LocalContext.current
    var formattedSizeGb by remember { mutableStateOf(32) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Panduan Format Kad MicroSD (Android)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Untuk membolehkan TWRP/OrangeFox membaca fail pemasangan Bliss OS pada Redmi 9T:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    // Step 1
                    MicroSdStep(
                        step = "1",
                        title = "Sistem Fail Format",
                        desc = "Formatkan MicroSD kepada FAT32 (kad ≤ 32GB) atau exFAT (kad ≥ 64GB)."
                    )
                    // Step 2
                    MicroSdStep(
                        step = "2",
                        title = "Struktur Folder",
                        desc = "Cipta folder `/sdcard1/bliss_rom/` dan letakkan fail `Bliss-OS.zip` di situ."
                    )
                    // Step 3
                    MicroSdStep(
                        step = "3",
                        title = "Fail Pilihan Tambahan",
                        desc = "Sertakan `Magisk-v27.apk` (tukar nama kepada `.zip`) untuk akses Root jika diperlukan."
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kalkulator Ruang Storan MicroSD",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Slider(
                        value = formattedSizeGb.toFloat(),
                        onValueChange = { formattedSizeGb = it.toInt() },
                        valueRange = 8f..128f,
                        steps = 7
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kapasiti Dipilih: ${formattedSizeGb} GB", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = if (formattedSizeGb >= 16) "✓ Mencukupi untuk ROM & Backup" else "⚠️ Disyorkan minimum 16GB",
                            color = if (formattedSizeGb >= 16) Color(0xFF10B981) else Color(0xFFF59E0B),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        item {
            WatermarkFooter()
        }
    }
}

@Composable
private fun MicroSdStep(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
private fun ChecksumValidatorTab(selectedRom: BlissRomVersion?) {
    var inputHash by remember { mutableStateOf("") }
    var validationResult by remember { mutableStateOf<Boolean?>(null) }

    val expectedHash = selectedRom?.sha256Checksum ?: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pengesah Integriti Fail (SHA256 Checksum)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Pastikan fail ROM yang dimuat turun tidak rosak atau corrupt sebelum diflash:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "SHA256 Rasmi (${selectedRom?.title ?: "Bliss OS 16"}):",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = expectedHash,
                            color = Color(0xFF22D3EE),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    OutlinedTextField(
                        value = inputHash,
                        onValueChange = { inputHash = it },
                        label = { Text("Tampal Hash Fail Anda Di Sini") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checksum_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            validationResult = inputHash.trim().equals(expectedHash.trim(), ignoreCase = true)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("validate_checksum_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sahkan Integriti Fail")
                    }

                    validationResult?.let { isValid ->
                        Surface(
                            color = if (isValid) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444)
                                )
                                Text(
                                    text = if (isValid) "PENGESAHAN LULUS: Fail ROM sempurna dan sedia diflash!" else "RALAT: Hash tidak sepadan! Sila muat turun semula fail ROM.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isValid) Color(0xFF10B981) else Color(0xFFEF4444)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            WatermarkFooter()
        }
    }
}
