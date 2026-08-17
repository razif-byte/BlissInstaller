package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AppBranding
import com.example.ui.components.*
import com.example.ui.theme.BlissOSTheme
import com.example.ui.viewmodel.BlissViewModel

sealed class AppDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : AppDestination("home", "Utama", Icons.Filled.Home, Icons.Outlined.Home)
    object Flashing : AppDestination("flashing", "Fastboot", Icons.Filled.Terminal, Icons.Outlined.Terminal)
    object Roms : AppDestination("roms", "ROM Bliss", Icons.Filled.DownloadForOffline, Icons.Outlined.DownloadForOffline)
    object WindowsSuite : AppDestination("windows", "Windows PC", Icons.Filled.LaptopWindows, Icons.Outlined.LaptopWindows)
    object Analytics : AppDestination("analytics", "Analitik", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Notifications : AppDestination("notifications", "Notifikasi", Icons.Filled.Notifications, Icons.Outlined.Notifications)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: BlissViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentDestination by remember { mutableStateOf<AppDestination>(AppDestination.Home) }

    val configuration = LocalConfiguration.current
    val isTabletOrExpanded = configuration.screenWidthDp >= 600

    val destinations = listOf(
        AppDestination.Home,
        AppDestination.Flashing,
        AppDestination.Roms,
        AppDestination.WindowsSuite,
        AppDestination.Analytics,
        AppDestination.Notifications
    )

    BlissOSTheme(darkTheme = uiState.isDarkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bliss_app_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column {
                                Text(
                                    text = "BlissOS Redmi 9T",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Installer & Fastboot Suite",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.toggleOwnerDialog(true) },
                            modifier = Modifier.testTag("owner_info_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Owner & Hubungi Kami",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Dark/Light Theme Switch
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Tukar Tema",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Notifications Badge
                        BadgedBox(
                            badge = {
                                if (uiState.unreadNotificationCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text("${uiState.unreadNotificationCount}")
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(
                                onClick = { currentDestination = AppDestination.Notifications },
                                modifier = Modifier.testTag("notifications_top_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifikasi"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isTabletOrExpanded) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        modifier = Modifier.testTag("main_bottom_navigation")
                    ) {
                        destinations.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.toggleAiChatSheet(true) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    text = { Text("Tanya Gemini AI") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("gemini_ai_fab")
                )
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Navigation Rail for Tablets / Wide screens
                if (isTabletOrExpanded) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        destinations.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.title
                                    )
                                },
                                label = { Text(dest.title, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Main Content Screen Box
                Box(modifier = Modifier.weight(1f)) {
                    when (currentDestination) {
                        AppDestination.Home -> HomeScreen(
                            uiState = uiState,
                            onPlatformModeChange = { viewModel.setPlatformMode(it) },
                            onReScanDevice = { viewModel.runAutoDeviceDetection() },
                            onNavigateToFlashing = { currentDestination = AppDestination.Flashing },
                            onNavigateToRoms = { currentDestination = AppDestination.Roms },
                            onNavigateToWindowsSuite = { currentDestination = AppDestination.WindowsSuite },
                            onNavigateToAnalytics = { currentDestination = AppDestination.Analytics },
                            onTriggerBackup = { viewModel.triggerAutoBackup() },
                            onCloudSync = { viewModel.syncCloudRealtime() },
                            onUnlockWarningClick = { viewModel.toggleUnlockWarningDialog(true) }
                        )

                        AppDestination.Flashing -> FlashingWizardScreen(
                            uiState = uiState,
                            onExecuteNextStep = { viewModel.executeNextFastbootStep() },
                            onExecuteAutomated = { viewModel.executeAllFastbootStepsAutomated() },
                            onTriggerBackup = { viewModel.triggerAutoBackup() },
                            onOpenExportReport = { viewModel.openExportDialog("PDF") }
                        )

                        AppDestination.Roms -> RomManagerScreen(
                            uiState = uiState,
                            onSelectRom = { viewModel.selectRom(it) }
                        )

                        AppDestination.WindowsSuite -> WindowsSuiteScreen(
                            uiState = uiState,
                            onOpenBatchScript = { viewModel.openExportDialog("BAT") }
                        )

                        AppDestination.Analytics -> AnalyticsScreen(
                            uiState = uiState,
                            onExportCsv = { viewModel.openExportDialog("CSV") },
                            onExportPdf = { viewModel.openExportDialog("PDF") },
                            onTriggerCloudSync = { viewModel.syncCloudRealtime() }
                        )

                        AppDestination.Notifications -> NotificationScreen(
                            uiState = uiState,
                            onMarkAsRead = { viewModel.markNotificationRead(it) },
                            onSendTestPush = { title, msg -> viewModel.addCustomPushNotification(title, msg) }
                        )
                    }
                }
            }
        }

        // Gemini AI Bottom Sheet
        if (uiState.showAiChatSheet) {
            AiChatBottomSheet(
                messages = uiState.chatMessages,
                isThinking = uiState.isAiThinking,
                onSendMessage = { viewModel.sendAiPrompt(it) },
                onDismiss = { viewModel.toggleAiChatSheet(false) }
            )
        }

        // Export Dialog (PDF/CSV/BAT)
        if (uiState.showExportDialog) {
            ExportDialog(
                exportType = uiState.exportType,
                content = uiState.exportDocumentContent,
                onDismiss = { viewModel.closeExportDialog() }
            )
        }

        // Owner & Contact Us Dialog
        if (uiState.showOwnerDialog) {
            OwnerContactDialog(
                onDismiss = { viewModel.toggleOwnerDialog(false) }
            )
        }

        // Unlock Risk Warning Modal
        if (uiState.showUnlockWarningDialog) {
            UnlockWarningModal(
                onAcknowledge = { viewModel.acknowledgeUnlockRisk() },
                onDismiss = { viewModel.toggleUnlockWarningDialog(false) }
            )
        }
    }
}
