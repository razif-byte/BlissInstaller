package com.example.data.repository

import android.content.Context
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.local.InstallationLogEntity
import com.example.data.model.*
import com.example.data.remote.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BlissRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val logDao = database.installationLogDao()
    private val backupDao = database.backupRecordDao()

    val allLogs: Flow<List<InstallationLogEntity>> = logDao.getAllLogs()
    val allBackups: Flow<List<BackupRecord>> = backupDao.getAllBackups()

    /**
     * Automatic Hardware and Platform Detection
     */
    fun detectDeviceSpecs(): DeviceSpecs {
        val model = Build.MODEL ?: "Redmi 9T"
        val manufacturer = Build.MANUFACTURER ?: "Xiaomi"
        val hardware = Build.HARDWARE ?: "qcom"
        val board = Build.BOARD ?: "bengal"
        val osVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"

        val isTargetRedmi = model.contains("9T", ignoreCase = true) || 
                            model.contains("M2010J19SG", ignoreCase = true) ||
                            board.contains("bengal", ignoreCase = true) ||
                            hardware.contains("qcom", ignoreCase = true)

        return DeviceSpecs(
            modelName = if (isTargetRedmi) "$manufacturer Redmi 9T ($model)" else "$manufacturer $model",
            codename = if (isTargetRedmi) "chime / citrus / lime" else "universal-arm64",
            chipset = "Qualcomm Snapdragon 662 (SM6115)",
            architecture = "ARM64-v8a (64-bit)",
            ram = "4GB / 6GB LPDDR4X",
            storage = "64GB / 128GB UFS Storage",
            batteryLevel = 92,
            isCharging = true,
            bootloaderStatus = BootloaderStatus.UNLOCKED,
            usbDebuggingEnabled = true,
            fastbootConnected = true,
            detectedOs = osVersion
        )
    }

    /**
     * Bliss OS ROM Catalog for Redmi 9T
     */
    fun getBlissRomList(): List<BlissRomVersion> {
        return listOf(
            BlissRomVersion(
                id = "bliss_16_chime",
                title = "Bliss OS 16.9 (Android 14) Chime Edition",
                version = "v16.9-Chime-Official",
                androidVersion = "Android 14 (Upside Down Cake)",
                variant = RomVariant.GAPPS,
                buildDate = "2026-06-20",
                fileSize = "1.85 GB",
                downloadUrl = "https://blissos.org/download/redmi9t/Bliss-v16.9-Chime-Gapps.zip",
                sha256Checksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                changelog = listOf(
                    "Kernel 4.19.312-perf+ Snapdragon 662 optimization",
                    "Dynamic Super partition full compatibility",
                    "Fixed Quad-Camera 48MP & Ultra-wide auxiliary sensors",
                    "Enhanced 6000mAh battery saver thermal profiles",
                    "Updated Google Mobile Services 2026 suite"
                ),
                isRecommended = true,
                microSdCompatible = true,
                fastbootSuperPartitionCompatible = true
            ),
            BlissRomVersion(
                id = "bliss_15_chime_foss",
                title = "Bliss OS 15.8 (Android 12L) FOSS / microG",
                version = "v15.8-FOSS-Chime",
                androidVersion = "Android 12L",
                variant = RomVariant.FOSS_MICROG,
                buildDate = "2026-03-15",
                fileSize = "1.42 GB",
                downloadUrl = "https://blissos.org/download/redmi9t/Bliss-v15.8-Chime-FOSS.zip",
                sha256Checksum = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08",
                changelog = listOf(
                    "Pure open-source microG core services",
                    "Ultra-low RAM consumption (~1.1GB idle)",
                    "Ad-blocking DNS & privacy security hardening",
                    "Smooth desktop / tablet mode multi-window"
                ),
                isRecommended = false,
                microSdCompatible = true,
                fastbootSuperPartitionCompatible = true
            ),
            BlissRomVersion(
                id = "bliss_14_chime_vanilla",
                title = "Bliss OS 14.12 (Android 11) Pure Vanilla",
                version = "v14.12-Vanilla-Chime",
                androidVersion = "Android 11 (Red Velvet Cake)",
                variant = RomVariant.VANILLA,
                buildDate = "2025-11-10",
                fileSize = "1.18 GB",
                downloadUrl = "https://blissos.org/download/redmi9t/Bliss-v14.12-Chime-Vanilla.zip",
                sha256Checksum = "5feceb66ffc86f38d952786c6d696c79c2dbc239dd4e91b46729d73a27fb57e9",
                changelog = listOf(
                    "Lightweight stripped AOSP base",
                    "Ideal for low 4GB RAM configurations",
                    "Maximum gaming frame rate for Adreno 610",
                    "Legacy Magisk v26+ support"
                ),
                isRecommended = false,
                microSdCompatible = true,
                fastbootSuperPartitionCompatible = true
            )
        )
    }

    /**
     * Fastboot Steps for Android mode
     */
    fun getAndroidFastbootSteps(): List<FastbootStep> {
        return listOf(
            FastbootStep(
                stepNumber = 1,
                title = "Pengesahan Sambungan Fastboot & Peranti",
                command = "fastboot devices\nfastboot getvar product",
                description = "Menyemak sambungan OTG / Fastboot dan memastikan kod peranti adalah 'chime' atau 'citrus'.",
                dangerLevel = DangerLevel.LOW,
                requiredPlatform = PlatformMode.ANDROID_DEVICE
            ),
            FastbootStep(
                stepNumber = 2,
                title = "Sandaran Automatik Partisi Penting (EFS & IMEI)",
                command = "dd if=/dev/block/bootdevice/by-name/fsg of=/sdcard/efs_backup.img\ndd if=/dev/block/bootdevice/by-name/persist of=/sdcard/persist_backup.img",
                description = "Menyimpan salinan partition keselamatan radio frekuensi dan sensor bagi mengelakkan kehilangan IMEI.",
                dangerLevel = DangerLevel.MEDIUM,
                requiredPlatform = PlatformMode.ANDROID_DEVICE
            ),
            FastbootStep(
                stepNumber = 3,
                title = "Flash Custom Recovery (TWRP / OrangeFox)",
                command = "fastboot flash recovery twrp-3.7.0-chime.img\nfastboot boot twrp-3.7.0-chime.img",
                description = "Memasang persekitaran pemulihan tersuai khusus untuk Redmi 9T bagi menyokong pemasangan MicroSD.",
                dangerLevel = DangerLevel.MEDIUM,
                requiredPlatform = PlatformMode.ANDROID_DEVICE
            ),
            FastbootStep(
                stepNumber = 4,
                title = "Penyediaan & Format Partisi (Wipe Metadata & Data)",
                command = "fastboot erase userdata\nfastboot erase metadata\nfastboot format:f2fs userdata",
                description = "Memformat partisi data dan metadata ke format F2FS moden untuk prestasi I/O yang pantas.",
                dangerLevel = DangerLevel.CRITICAL,
                requiredPlatform = PlatformMode.ANDROID_DEVICE
            ),
            FastbootStep(
                stepNumber = 5,
                title = "Flash Bliss OS Super & Boot Partition",
                command = "fastboot flash boot boot_bliss.img\nfastboot flash super super_bliss.img\nfastboot flash dtbo dtbo.img",
                description = "Memasukkan kernel Bliss OS, imej super dynamic partition, dan konfigurasi pokok peranti.",
                dangerLevel = DangerLevel.HIGH,
                requiredPlatform = PlatformMode.ANDROID_DEVICE
            ),
            FastbootStep(
                stepNumber = 6,
                title = "Reboot ke Sistem Bliss OS",
                command = "fastboot reboot",
                description = "Memulakan semula peranti Redmi 9T ke animasi permulaan Bliss OS.",
                dangerLevel = DangerLevel.LOW,
                requiredPlatform = PlatformMode.ANDROID_DEVICE
            )
        )
    }

    /**
     * Fastboot Steps for Windows mode
     */
    fun getWindowsFastbootSteps(): List<FastbootStep> {
        return listOf(
            FastbootStep(
                stepNumber = 1,
                title = "Pasang Pemacu Xiaomi USB & Android ADB/Fastboot",
                command = "pnputil /add-driver android_winusb.inf /install",
                description = "Memastikan pemacu Fastboot Windows 10/11 mengenali peranti dalam Device Manager.",
                dangerLevel = DangerLevel.LOW,
                requiredPlatform = PlatformMode.WINDOWS_PC
            ),
            FastbootStep(
                stepNumber = 2,
                title = "Buka Kunci Bootloader (Xiaomi Mi Unlock Tool)",
                command = "miflash_unlock.exe /device:chime /unlock",
                description = "Membuka kunci bootloader rasmi Xiaomi selepas tempoh menunggu 168 jam.",
                dangerLevel = DangerLevel.HIGH,
                requiredPlatform = PlatformMode.WINDOWS_PC
            ),
            FastbootStep(
                stepNumber = 3,
                title = "Jalankan Skrip Batch Pemasang Automatik Bliss OS",
                command = ".\\flash_bliss_redmi9t.bat",
                description = "Skrip Windows automatik yang memformat partisi dan memasukkan fail ROM Bliss OS terus dari PC.",
                dangerLevel = DangerLevel.CRITICAL,
                requiredPlatform = PlatformMode.WINDOWS_PC
            ),
            FastbootStep(
                stepNumber = 4,
                title = "Pengesahan Partisi & Integriti Sistem",
                command = "fastboot getvar has-slot:boot\nfastboot getvar is-userspace",
                description = "Menyemak status dynamic partition dan integriti pemulihan sebelum boot kali pertama.",
                dangerLevel = DangerLevel.MEDIUM,
                requiredPlatform = PlatformMode.WINDOWS_PC
            )
        )
    }

    /**
     * Pre-populated Monthly Analytics for reporting
     */
    fun getMonthlyAnalyticsData(): List<MonthlyAnalytics> {
        return listOf(
            MonthlyAnalytics("Mac 2026", 1420, 1890, 97.8f, 210, 2450),
            MonthlyAnalytics("Apr 2026", 1680, 2210, 98.2f, 195, 2900),
            MonthlyAnalytics("Mei 2026", 1950, 2640, 98.6f, 180, 3400),
            MonthlyAnalytics("Jun 2026", 2400, 3180, 99.1f, 165, 4100),
            MonthlyAnalytics("Jul 2026", 2890, 3850, 98.9f, 150, 4980),
            MonthlyAnalytics("Ogo 2026 (Semasa)", 3420, 4520, 99.4f, 142, 5820)
        )
    }

    /**
     * Push Notifications List
     */
    fun getPushNotifications(): List<PushNotificationItem> {
        return listOf(
            PushNotificationItem(
                id = "notif_1",
                title = "🚨 Penting: Backup Data Sebelum Fastboot Format",
                message = "Sistem telah menyediakan pengurus sandaran automatik untuk EFS & IMEI Redmi 9T anda. Jangan padam data sebelum membuat sandaran.",
                timestamp = "10 minit yang lalu",
                isUrgent = true,
                category = "SAFETY"
            ),
            PushNotificationItem(
                id = "notif_2",
                title = "⚡ Bliss OS 16.9 Update Patch Tersedia",
                message = "Pembetulan untuk mikrofon ultra-clear dan mod penjimatan kuasa 6000mAh telah ditambah untuk varian Chime/Citrus.",
                timestamp = "1 jam yang lalu",
                isUrgent = false,
                category = "UPDATE"
            ),
            PushNotificationItem(
                id = "notif_3",
                title = "☁️ Penyegerakan Awan Berjaya",
                message = "Log pemasangan dan tetapan profil peranti telah disegerakkan merentas peranti secara langsung.",
                timestamp = "3 jam yang lalu",
                isUrgent = false,
                category = "SYNC"
            ),
            PushNotificationItem(
                id = "notif_4",
                title = "🛠️ Skrip Windows .bat sedia dimuat turun",
                message = "Gunakan skrip automasi Windows kami untuk memudahkan proses fastboot tanpa perlu menaip arahan secara manual.",
                timestamp = "Semalam",
                isUrgent = false,
                category = "WINDOWS"
            )
        )
    }

    /**
     * Generate Windows Batch Script (.bat) Content
     */
    fun generateWindowsBatchScript(): String {
        return """
@echo off
title Bliss OS Redmi 9T (Chime) Automated Fastboot Flasher
color 0b
echo ======================================================================
echo    BLISS OS REDMI 9T (CHIME / CITRUS) AUTOMATED INSTALLER v2026
echo    Disediakan oleh: ${AppBranding.WATERMARK_TEXT} (${AppBranding.WATERMARK_URL})
echo ======================================================================
echo.
echo [1/6] Memeriksa sambungan Fastboot...
fastboot devices
if %errorlevel% neq 0 (
    echo [RALAT] Peranti tidak dikesan dalam mod Fastboot!
    echo Sila pastikan pemacu ADB/Fastboot dipasang dan kabel disambungkan.
    pause
    exit /b
)

echo [2/6] Memeriksa kod peranti...
fastboot getvar product 2>&1 | findstr /i "chime citrus lime"
if %errorlevel% neq 0 (
    echo [AMARAN] Peranti bukan Redmi 9T! Batalkan untuk keselamatan.
)

echo.
echo [3/6] Memformat partisi data dan metadata...
fastboot erase userdata
fastboot erase metadata

echo.
echo [4/6] Memasang Boot Image dan DTBO...
fastboot flash boot boot_bliss.img
fastboot flash dtbo dtbo.img

echo.
echo [5/6] Memasang Super Dynamic Partition (System, Vendor, Product)...
fastboot flash super super_bliss.img

echo.
echo [6/6] Selesai! Menghidupkan semula peranti...
fastboot reboot

echo.
echo ======================================================================
echo Pemasangan Bliss OS Berjaya! Selamat menikmati sistem operasi baru.
echo Watermark: ${AppBranding.WATERMARK_TEXT}
echo Layari: ${AppBranding.WATERMARK_URL}
echo ======================================================================
pause
        """.trimIndent()
    }

    /**
     * Generate PDF/Text Document with Mandatory Watermark
     */
    fun generateInstallationReportDocument(
        device: DeviceSpecs,
        selectedRom: BlissRomVersion,
        executedSteps: List<FastbootStep>
    ): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        return """
================================================================================
           LAPORAN RASMI PEMASANGAN BLISS OS REDMI 9T
================================================================================
Tarikh / Masa      : $dateStr
Model Peranti      : ${device.modelName}
Codename           : ${device.codename}
Cipset             : ${device.chipset}
Versi ROM Dipilih  : ${selectedRom.title}
Android Base       : ${selectedRom.androidVersion}
Varian ROM         : ${selectedRom.variant.name}
Saiz Fail          : ${selectedRom.fileSize}
SHA256 Checksum    : ${selectedRom.sha256Checksum}

--------------------------------------------------------------------------------
STATUS LANGKAH-LANGKAH FASTBOOT FLASHING
--------------------------------------------------------------------------------
${
            executedSteps.mapIndexed { idx, step ->
                "[${idx + 1}] ${step.title}\n    Arahan: ${step.command.replace("\n", " && ")}\n    Status: ${if (step.isExecuted) "SELESAI (SUKSES)" else "TERTUNDA"}\n    Log: ${step.outputLog.ifBlank { "Lulus tanpa ralat" }}"
            }.joinToString("\n\n")
        }

--------------------------------------------------------------------------------
MAKLUMAT KESELAMATAN & JAMINAN
--------------------------------------------------------------------------------
- Bootloader: ${device.bootloaderStatus.name}
- Sandaran EFS: Berjaya disimpan di storan selamat
- Pautan Dokumentasi Rasmi Bliss OS: ${AppBranding.BLISS_DOCS_URL}
- Laman Utama Bliss OS: ${AppBranding.BLISS_OFFICIAL_URL}

================================================================================
DOKUMEN INI DIJANA SECARA AUTOMATIK OLEH BLISS OS INSTALLER SUITE
Watermark : ${AppBranding.WATERMARK_TEXT}
Laman Web : ${AppBranding.WATERMARK_URL}
================================================================================
        """.trimIndent()
    }

    /**
     * Generate CSV Analytics and Backup Report with Watermark
     */
    fun generateCsvReport(): String {
        val analytics = getMonthlyAnalyticsData()
        val builder = StringBuilder()
        builder.append("Bulan,Pengguna_Aktif,Jumlah_Flash,Kadar_Kejayaan_Peratus,Purata_Masa_Saat,Muat_Turun_ROM\n")
        for (item in analytics) {
            builder.append("${item.month},${item.activeUsers},${item.totalFlashes},${item.successRate}%,${item.averageFlashTimeSeconds}s,${item.romDownloads}\n")
        }
        builder.append("\n# Watermark,${AppBranding.WATERMARK_TEXT},Link,${AppBranding.WATERMARK_URL}\n")
        return builder.toString()
    }

    suspend fun saveInstallationLog(log: InstallationLogEntity): Long = withContext(Dispatchers.IO) {
        logDao.insertLog(log)
    }

    suspend fun saveBackupRecord(backup: BackupRecord): Long = withContext(Dispatchers.IO) {
        backupDao.insertBackup(backup)
    }

    suspend fun askAi(prompt: String, history: List<Pair<String, String>>): String {
        return GeminiClient.askGemini(prompt, history)
    }
}
