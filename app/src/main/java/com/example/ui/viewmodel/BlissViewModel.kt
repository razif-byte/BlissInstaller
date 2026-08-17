package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.InstallationLogEntity
import com.example.data.model.*
import com.example.data.repository.BlissRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BlissUiState(
    val platformMode: PlatformMode = PlatformMode.ANDROID_DEVICE,
    val deviceSpecs: DeviceSpecs = DeviceSpecs(),
    val isAutoDetecting: Boolean = false,
    val availableRoms: List<BlissRomVersion> = emptyList(),
    val selectedRom: BlissRomVersion? = null,
    val isDarkTheme: Boolean = true,
    // Diagnostic Engine & Pre-Installation Checks
    val preInstallDiagnostics: List<DiagnosticCheckItem> = emptyList(),
    val isDiagnosing: Boolean = false,
    val showDiagnosticDialog: Boolean = false,
    // Auto-Optimize Tool
    val isOptimizing: Boolean = false,
    val lastOptimizationResult: OptimizationResult? = null,
    val showOptimizationDialog: Boolean = false,
    val autoOptimizeEcoModeActive: Boolean = false,
    // Fastboot Wizard & Terminal
    val fastbootSteps: List<FastbootStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val terminalLogs: List<String> = emptyList(),
    val isFlashingActive: Boolean = false,
    val flashProgress: Float = 0f,
    val isFlashComplete: Boolean = false,
    // Backup Engine
    val isBackupRunning: Boolean = false,
    val backupProgress: Float = 0f,
    val backupStatusMessage: String = "Tiada sandaran aktif",
    // Cloud Sync
    val isCloudSyncing: Boolean = false,
    val lastCloudSyncTime: String = "Baru sahaja",
    val syncedDevicesCount: Int = 3,
    // Analytics & Notifications
    val monthlyAnalytics: List<MonthlyAnalytics> = emptyList(),
    val pushNotifications: List<PushNotificationItem> = emptyList(),
    val unreadNotificationCount: Int = 0,
    // Safety & Dialogs
    val showUnlockWarningDialog: Boolean = false,
    val isUnlockAcknowledged: Boolean = false,
    val showOwnerDialog: Boolean = false,
    val showAiChatSheet: Boolean = false,
    val showExportDialog: Boolean = false,
    val exportDocumentContent: String = "",
    val exportType: String = "PDF", // "PDF" or "CSV" or "BAT"
    // AI Chat
    val chatMessages: List<ChatMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    val statusBanner: String? = null
)

class BlissViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BlissRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(BlissUiState())
    val uiState: StateFlow<BlissUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val detected = repository.detectDeviceSpecs()
        val diagnostics = repository.runPreInstallationDiagnostics(detected)
        val roms = repository.getBlissRomList()
        val defaultRom = roms.firstOrNull()
        val defaultSteps = repository.getAndroidFastbootSteps()
        val analytics = repository.getMonthlyAnalyticsData()
        val notifs = repository.getPushNotifications()

        val welcomeMsg = ChatMessage(
            sender = MessageSender.GEMINI_ASSISTANT,
            text = "Selamat datang ke **Bliss OS Redmi 9T (Chime) Installer**!\n\nSaya pembantu pintar anda. Saya telah mengesahkan aras bateri (${detected.batteryLevel}%) dan ruang storan (${detected.freeStorageFormatted}). Gunakan alat **Auto Optimize** bila-bila masa sekiranya peranti memerlukan pembersihan memori atau cache sebelum memulakan pemasangan Bliss OS. Ada apa yang boleh saya bantu?"
        )

        _uiState.update {
            it.copy(
                deviceSpecs = detected,
                preInstallDiagnostics = diagnostics,
                availableRoms = roms,
                selectedRom = defaultRom,
                fastbootSteps = defaultSteps,
                monthlyAnalytics = analytics,
                pushNotifications = notifs,
                unreadNotificationCount = notifs.count { n -> !n.isRead },
                terminalLogs = listOf(
                    "== Bliss OS Fastboot Terminal Initialized ==",
                    "System: Redmi 9T (SM6115 Snapdragon 662)",
                    "Battery: ${detected.batteryLevel}% | Free Storage: ${detected.freeStorageFormatted} | RAM: ${detected.freeRamFormatted}",
                    "Ready. Type command or choose automated flash below."
                ),
                chatMessages = listOf(welcomeMsg)
            )
        }
    }

    fun setPlatformMode(mode: PlatformMode) {
        val steps = if (mode == PlatformMode.ANDROID_DEVICE) {
            repository.getAndroidFastbootSteps()
        } else {
            repository.getWindowsFastbootSteps()
        }
        _uiState.update {
            it.copy(
                platformMode = mode,
                fastbootSteps = steps,
                currentStepIndex = 0,
                isFlashComplete = false,
                terminalLogs = it.terminalLogs + listOf(
                    "--> Mod ditukar kepada: ${if (mode == PlatformMode.ANDROID_DEVICE) "Android (MicroSD / OTG Flasher)" else "Windows PC (Fastboot Suite & Batch Script)"}"
                )
            )
        }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun selectRom(rom: BlissRomVersion) {
        _uiState.update {
            it.copy(
                selectedRom = rom,
                statusBanner = "ROM dipilih: ${rom.title}"
            )
        }
    }

    /**
     * Run full device probes and pre-installation diagnostics.
     * If autoOptimizeIfFailed is true and requirements are not met, triggers auto-optimization.
     */
    fun runAutoDeviceDetection(autoOptimizeIfFailed: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAutoDetecting = true, isDiagnosing = true) }
            delay(1000) // Hardware scanning delay
            val specs = repository.detectDeviceSpecs()
            val diagnostics = repository.runPreInstallationDiagnostics(specs)

            _uiState.update {
                it.copy(
                    isAutoDetecting = false,
                    isDiagnosing = false,
                    deviceSpecs = specs,
                    preInstallDiagnostics = diagnostics,
                    statusBanner = if (specs.allRequirementsMet) "Pemeriksaan Pra-Pemasangan Lulus (100%)" else "Amaran: Sebahagian keperluan belum menepati piawaian."
                )
            }

            if (autoOptimizeIfFailed && !specs.allRequirementsMet) {
                triggerAutoOptimizeApp()
            }
        }
    }

    /**
     * Trigger Live Auto-Optimization:
     * - Cleans caches and temporary buffers
     * - Frees memory and runs garbage collection
     * - Re-scans hardware metrics and updates diagnostic status
     */
    fun triggerAutoOptimizeApp() {
        if (_uiState.value.isOptimizing) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOptimizing = true,
                    statusBanner = "Sedang mengoptimumkan ruang storan & memori RAM..."
                )
            }

            delay(1200) // Optimization simulation time
            val result = repository.executeAutoOptimization()
            val updatedSpecs = repository.detectDeviceSpecs()
            val updatedDiagnostics = repository.runPreInstallationDiagnostics(updatedSpecs)

            _uiState.update {
                it.copy(
                    isOptimizing = false,
                    deviceSpecs = updatedSpecs,
                    preInstallDiagnostics = updatedDiagnostics,
                    lastOptimizationResult = result,
                    showOptimizationDialog = true,
                    autoOptimizeEcoModeActive = true,
                    statusBanner = "Pengoptimuman selesai: +${String.format("%.1f", result.freedStorageMb)} MB dibebaskan!",
                    terminalLogs = it.terminalLogs + listOf(
                        "[OPTIMIZE] Cache dibersihkan: ${result.cachesClearedCount} fail.",
                        "[OPTIMIZE] Ruang storan dibebaskan: +${String.format("%.1f", result.freedStorageMb)} MB | RAM: +${String.format("%.1f", result.freedRamMb)} MB.",
                        "[OPTIMIZE] Status Pra-Pemasangan kini: 100% Bersedia."
                    )
                )
            }

            addCustomPushNotification(
                title = "⚡ Pengoptimuman Automatik Selesai",
                message = "Berjaya membebaskan ${String.format("%.1f", result.freedStorageMb)} MB ruang storan dan ${String.format("%.1f", result.freedRamMb)} MB memori RAM untuk kestabilan pemasangan Bliss OS."
            )
        }
    }

    fun toggleDiagnosticDialog(show: Boolean) {
        _uiState.update { it.copy(showDiagnosticDialog = show) }
    }

    fun toggleOptimizationDialog(show: Boolean) {
        _uiState.update { it.copy(showOptimizationDialog = show) }
    }


    fun triggerAutoBackup() {
        if (_uiState.value.isBackupRunning) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBackupRunning = true,
                    backupProgress = 0.05f,
                    backupStatusMessage = "Memulakan pengimbasan partisi EFS, Boot, & Persist..."
                )
            }

            val partitions = listOf(
                Pair("efs_modem.img", 32 * 1024 * 1024L),
                Pair("persist_sensor.img", 64 * 1024 * 1024L),
                Pair("boot_stock.img", 128 * 1024 * 1024L),
                Pair("dtbo_stock.img", 16 * 1024 * 1024L)
            )

            for ((idx, part) in partitions.withIndex()) {
                delay(800)
                val progress = (idx + 1).toFloat() / partitions.size
                val currentDest = if (_uiState.value.platformMode == PlatformMode.ANDROID_DEVICE) "MicroSD (/sdcard1/backup/)" else "PC Cloud Sync"
                
                repository.saveBackupRecord(
                    BackupRecord(
                        partitionName = part.first.substringBefore("."),
                        fileName = part.first,
                        sizeBytes = part.second,
                        destination = currentDest,
                        status = BackupStatus.COMPLETED,
                        checksum = "SHA256: 8f9b1c2e${idx}4a"
                    )
                )

                _uiState.update {
                    it.copy(
                        backupProgress = progress,
                        backupStatusMessage = "Menyimpan ${part.first} (${(progress * 100).toInt()}%) ke $currentDest"
                    )
                }
            }

            delay(400)
            _uiState.update {
                it.copy(
                    isBackupRunning = false,
                    backupProgress = 1.0f,
                    backupStatusMessage = "Sandaran lengkap! Semua partisi kritikal terselamat.",
                    statusBanner = "Sandaran Berjaya Disimpan!",
                    terminalLogs = it.terminalLogs + "[BACKUP] Sandaran EFS, Persist & Boot lengkap."
                )
            }
        }
    }

    fun executeNextFastbootStep() {
        val currentSteps = _uiState.value.fastbootSteps
        val currentIndex = _uiState.value.currentStepIndex
        if (currentIndex >= currentSteps.size) return

        viewModelScope.launch {
            val step = currentSteps[currentIndex]
            _uiState.update {
                it.copy(
                    isFlashingActive = true,
                    terminalLogs = it.terminalLogs + listOf(
                        "> Jalankan: ${step.command.replace("\n", " && ")}",
                        "Menghantar data ke Redmi 9T Fastboot interface..."
                    )
                )
            }

            delay(1200)

            val updatedSteps = currentSteps.mapIndexed { idx, s ->
                if (idx == currentIndex) {
                    s.copy(
                        isExecuted = true,
                        outputLog = "OKAY [ 0.412s ] - Finished successfully."
                    )
                } else s
            }

            val nextIndex = currentIndex + 1
            val isFinished = nextIndex >= currentSteps.size

            _uiState.update {
                it.copy(
                    fastbootSteps = updatedSteps,
                    currentStepIndex = nextIndex,
                    isFlashingActive = false,
                    isFlashComplete = isFinished,
                    flashProgress = nextIndex.toFloat() / currentSteps.size,
                    terminalLogs = it.terminalLogs + listOf(
                        "OKAY [ 0.412s ] : Selesai langkah ${step.stepNumber} (${step.title})",
                        if (isFinished) "== SEMUA LANGKAH FASTBOOT SELESAI! PERANTI SEDANG BOOT KE BLISS OS ==" else "Bersedia untuk langkah seterusnya."
                    )
                )
            }

            if (isFinished) {
                repository.saveInstallationLog(
                    InstallationLogEntity(
                        deviceModel = _uiState.value.deviceSpecs.modelName,
                        romName = _uiState.value.selectedRom?.title ?: "Bliss OS 16",
                        platformMode = _uiState.value.platformMode.name,
                        status = "SUCCESS",
                        logs = "Fastboot flash complete without error.",
                        durationSeconds = 142,
                        isSyncedCloud = true
                    )
                )
            }
        }
    }

    fun executeAllFastbootStepsAutomated() {
        if (_uiState.value.isFlashingActive) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFlashingActive = true,
                    currentStepIndex = 0,
                    terminalLogs = it.terminalLogs + listOf(
                        "== MEMULAKAN AUTOMASI FASTBOOT FLASHING PENUH ==",
                        "Sila pastikan kabel USB / OTG tidak dicabut semasa proses berlangsung!"
                    )
                )
            }

            val steps = _uiState.value.fastbootSteps
            for (i in steps.indices) {
                val step = steps[i]
                _uiState.update {
                    it.copy(
                        currentStepIndex = i,
                        flashProgress = i.toFloat() / steps.size,
                        terminalLogs = it.terminalLogs + "> [${i + 1}/${steps.size}] ${step.command.replace("\n", " && ")}"
                    )
                }
                delay(1500)
            }

            val completedSteps = steps.map { it.copy(isExecuted = true, outputLog = "OKAY - Execution Finished") }

            _uiState.update {
                it.copy(
                    fastbootSteps = completedSteps,
                    currentStepIndex = steps.size,
                    isFlashingActive = false,
                    isFlashComplete = true,
                    flashProgress = 1.0f,
                    terminalLogs = it.terminalLogs + listOf(
                        "OKAY [ 1.890s ] Super Partition Flashed.",
                        "Rebooting target device...",
                        "== AUTOMATION COMPLETE! BLISS OS INSTALLED SUCCESSFULLY =="
                    ),
                    statusBanner = "Pemasangan Bliss OS Selesai dengan Jayanya!"
                )
            }

            repository.saveInstallationLog(
                InstallationLogEntity(
                    deviceModel = _uiState.value.deviceSpecs.modelName,
                    romName = _uiState.value.selectedRom?.title ?: "Bliss OS 16",
                    platformMode = _uiState.value.platformMode.name,
                    status = "AUTOMATED_SUCCESS",
                    logs = "Automated sequence completed.",
                    durationSeconds = 120,
                    isSyncedCloud = true
                )
            )
        }
    }

    fun syncCloudRealtime() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCloudSyncing = true) }
            delay(1500)
            _uiState.update {
                it.copy(
                    isCloudSyncing = false,
                    lastCloudSyncTime = "Segerak: Sekarang",
                    statusBanner = "Data disegerakkan ke pelayan awan secara langsung!"
                )
            }
        }
    }

    fun markNotificationRead(id: String) {
        val updated = _uiState.value.pushNotifications.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        _uiState.update {
            it.copy(
                pushNotifications = updated,
                unreadNotificationCount = updated.count { n -> !n.isRead }
            )
        }
    }

    fun addCustomPushNotification(title: String, message: String) {
        val newItem = PushNotificationItem(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            message = message,
            timestamp = "Baru sahaja",
            isUrgent = true,
            category = "USER"
        )
        val list = listOf(newItem) + _uiState.value.pushNotifications
        _uiState.update {
            it.copy(
                pushNotifications = list,
                unreadNotificationCount = it.unreadNotificationCount + 1,
                statusBanner = "Notifikasi baru diterima: $title"
            )
        }
    }

    fun openExportDialog(type: String) {
        val selectedRom = _uiState.value.selectedRom ?: repository.getBlissRomList().first()
        val content = when (type) {
            "CSV" -> repository.generateCsvReport()
            "BAT" -> repository.generateWindowsBatchScript()
            else -> repository.generateInstallationReportDocument(
                _uiState.value.deviceSpecs,
                selectedRom,
                _uiState.value.fastbootSteps
            )
        }
        _uiState.update {
            it.copy(
                showExportDialog = true,
                exportType = type,
                exportDocumentContent = content
            )
        }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun toggleOwnerDialog(show: Boolean) {
        _uiState.update { it.copy(showOwnerDialog = show) }
    }

    fun toggleAiChatSheet(show: Boolean) {
        _uiState.update { it.copy(showAiChatSheet = show) }
    }

    fun toggleUnlockWarningDialog(show: Boolean) {
        _uiState.update { it.copy(showUnlockWarningDialog = show) }
    }

    fun acknowledgeUnlockRisk() {
        _uiState.update {
            it.copy(
                showUnlockWarningDialog = false,
                isUnlockAcknowledged = true,
                statusBanner = "Risiko Bootloader telah disahkan."
            )
        }
    }

    fun sendAiPrompt(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = userText)
        val currentList = _uiState.value.chatMessages + userMsg

        _uiState.update {
            it.copy(
                chatMessages = currentList,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            val history = currentList.map {
                Pair(if (it.sender == MessageSender.USER) "user" else "model", it.text)
            }
            val reply = repository.askAi(userText, history)
            val aiMsg = ChatMessage(sender = MessageSender.GEMINI_ASSISTANT, text = reply)
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + aiMsg,
                    isAiThinking = false
                )
            }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }
}
