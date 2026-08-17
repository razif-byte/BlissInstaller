package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Platform targets supported by the installer
 */
enum class PlatformMode {
    ANDROID_DEVICE,
    WINDOWS_PC
}

/**
 * Hardware specifications and diagnostic state of the target device
 */
data class DeviceSpecs(
    val modelName: String = "Xiaomi Redmi 9T",
    val codename: String = "chime / citrus / lime",
    val chipset: String = "Qualcomm Snapdragon 662 (SM6115)",
    val architecture: String = "ARM64 (aarch64)",
    val ram: String = "4GB / 6GB LPDDR4X",
    val storage: String = "64GB / 128GB UFS 2.1 / 2.2",
    val batteryLevel: Int = 88,
    val isCharging: Boolean = true,
    val bootloaderStatus: BootloaderStatus = BootloaderStatus.LOCKED,
    val usbDebuggingEnabled: Boolean = true,
    val fastbootConnected: Boolean = true,
    val detectedOs: String = "Android 12 (MIUI 13 / HyperOS Compatible)"
)

enum class BootloaderStatus {
    LOCKED,
    UNLOCKED,
    UNLOCKING_IN_PROGRESS,
    RELOCKED
}

/**
 * Bliss OS ROM Catalog item
 */
data class BlissRomVersion(
    val id: String,
    val title: String,
    val version: String,
    val androidVersion: String,
    val variant: RomVariant,
    val buildDate: String,
    val fileSize: String,
    val downloadUrl: String,
    val sha256Checksum: String,
    val changelog: List<String>,
    val isRecommended: Boolean = false,
    val microSdCompatible: Boolean = true,
    val fastbootSuperPartitionCompatible: Boolean = true
)

enum class RomVariant {
    GAPPS,
    FOSS_MICROG,
    VANILLA
}

/**
 * Backup item representation
 */
@Entity(tableName = "backup_records")
data class BackupRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partitionName: String,
    val fileName: String,
    val sizeBytes: Long,
    val destination: String, // "MicroSD", "Internal Storage", "Cloud Sync"
    val timestamp: Long = System.currentTimeMillis(),
    val status: BackupStatus = BackupStatus.COMPLETED,
    val checksum: String = ""
)

enum class BackupStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

/**
 * Fastboot Execution Step
 */
data class FastbootStep(
    val stepNumber: Int,
    val title: String,
    val command: String,
    val description: String,
    val dangerLevel: DangerLevel = DangerLevel.MEDIUM,
    val requiredPlatform: PlatformMode = PlatformMode.ANDROID_DEVICE,
    var isExecuted: Boolean = false,
    var outputLog: String = ""
)

enum class DangerLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Push Notification Model
 */
data class PushNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isUrgent: Boolean = false,
    val category: String = "SYSTEM",
    var isRead: Boolean = false
)

/**
 * Monthly analytics snapshot
 */
data class MonthlyAnalytics(
    val month: String,
    val activeUsers: Int,
    val totalFlashes: Int,
    val successRate: Float,
    val averageFlashTimeSeconds: Int,
    val romDownloads: Int
)

/**
 * AI Message structure
 */
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isThinking: Boolean = false
)

enum class MessageSender {
    USER,
    GEMINI_ASSISTANT
}

/**
 * Watermark & Branding constant values
 */
object AppBranding {
    const val WATERMARK_TEXT = "RazifAppStudios@nasadef"
    const val WATERMARK_URL = "https://nasadef.com.my"
    const val OWNER_PHOTO_URL = "https://lh3.googleusercontent.com/d/1KcGLfnvnrf4R0-q-7V9RAv1eE1o3JMe8"
    const val OWNER_DRIVE_LINK = "https://drive.google.com/file/d/1KcGLfnvnrf4R0-q-7V9RAv1eE1o3JMe8/view?usp=drive_link"
    const val APP_LOGO_URL = "https://lh3.googleusercontent.com/d/15ZTftcpBvRURnIoO7l2SBnKH2vqm0oBs"
    const val APP_LOGO_DRIVE_LINK = "https://drive.google.com/file/d/15ZTftcpBvRURnIoO7l2SBnKH2vqm0oBs/view?usp=drive_link"
    const val BLISS_DOCS_URL = "https://docs.blissos.org"
    const val BLISS_OFFICIAL_URL = "https://blissos.org"
    const val REDMI_9T_CODENAME = "chime / citrus"
}
