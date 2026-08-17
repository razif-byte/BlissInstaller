package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = "user",
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.9f
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContentResponse? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContentResponse(
    @Json(name = "parts") val parts: List<GeminiPart>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askGemini(prompt: String, conversationHistory: List<Pair<String, String>> = emptyList()): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineIntelligentResponse(prompt)
        }

        val systemPrompt = "Anda adalah Pakar Teknikal AI untuk Pemasangan Bliss OS pada Xiaomi Redmi 9T (Codename: Chime / Citrus / Lime) dengan SoC Snapdragon 662. Anda membantu pengguna mengenai Fastboot, Unlock Bootloader (Mi Unlock 168j), partisi Dynamic/Super partition, TWRP/OrangeFox recovery, Magisk root, MicroSD preparation, script Windows .bat, dan mengatasi masalah bootloop/brick. Berikan jawapan yang berstruktur, selamat, tepat, mesra pengguna dan dalam Bahasa Melayu atau Bahasa Inggeris mengikut bahasa pengguna."

        val contentsList = mutableListOf<GeminiContent>()
        for ((sender, text) in conversationHistory.takeLast(6)) {
            val role = if (sender == "user") "user" else "model"
            contentsList.add(GeminiContent(role = role, parts = listOf(GeminiPart(text = text))))
        }
        contentsList.add(GeminiContent(role = "user", parts = listOf(GeminiPart(text = prompt))))

        val request = GeminiRequest(
            contents = contentsList,
            systemInstruction = GeminiContent(role = "system", parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenConfig(temperature = 0.7f, topP = 0.9f)
        )

        try {
            val response = service.generateContent(apiKey, request)
            val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            reply ?: getOfflineIntelligentResponse(prompt)
        } catch (e: Exception) {
            getOfflineIntelligentResponse(prompt)
        }
    }

    private fun getOfflineIntelligentResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            "fastboot" in lower || "command" in lower -> """
                💡 **Panduan Perintah Fastboot untuk Redmi 9T (Chime):**
                1. Semak sambungan: `fastboot devices`
                2. Semak status peranti: `fastboot getvar product` (Mesti memaparkan *chime* atau *citrus*)
                3. Flash Custom Recovery: `fastboot flash recovery twrp-chime.img`
                4. Flash Boot image: `fastboot flash boot boot.img`
                5. Wipe format data: `fastboot erase userdata`
                6. Reboot ke sistem: `fastboot reboot`
                
                ⚠️ *Pastikan bateri peranti melebihi 60% sebelum memulakan.*
            """.trimIndent()

            "unlock" in lower || "bootloader" in lower -> """
                🔓 **Langkah Membuka Bootloader (Unlock Bootloader) Redmi 9T:**
                1. Masuk ke **Settings > About Phone** dan ketik **MIUI Version** sebanyak 7 kali.
                2. Buka **Developer Options**, aktifkan **OEM Unlocking** & **USB Debugging**.
                3. Masuk ke **Mi Unlock Status**, kaitkan Mi Account anda bersama kad SIM aktif.
                4. Tunggu tempoh kelulusan Xiaomi (kebiasaannya 168 jam / 7 hari).
                5. Muat turun aplikasi Mi Unlock di PC Windows, masuk ke mod Fastboot (Volume Down + Power) dan klik **Unlock**.
                
                ⚠️ *AMARAN: Proses ini akan memadam semua data (Factory Reset).*
            """.trimIndent()

            "microsd" in lower || "sd card" in lower || "format" in lower -> """
                💾 **Penyediaan Fail Bliss OS pada MicroSD (Android):**
                1. Formatkan MicroSD ke sistem fail **FAT32** atau **exFAT** (Minimum 8GB disyorkan).
                2. Salin fail ROM `Bliss-OS-Chime.zip` dan `Magisk.zip` ke direktori akar `/sdcard1/`.
                3. Masuk ke Recovery (TWRP / OrangeFox) dengan menekan Volume Up + Power.
                4. Pilih **Install > Select Storage > MicroSD Card**, pilih fail Bliss OS dan lakukan Swipe to Flash.
            """.trimIndent()

            "brick" in lower || "bootloop" in lower || "masalah" in lower || "error" in lower -> """
                🚨 **Penyelesaian Bootloop / Brick Redmi 9T:**
                1. **Soft Bootloop:** Masuk semula ke Fastboot (Volume Down + Power), jalankan `fastboot erase metadata` dan `fastboot erase userdata`.
                2. **Recovery Loop:** Flash semula stock recovery atau TWRP terkini melalui fastboot.
                3. **Hard Brick (Skrin Hitam / EDL Mode):** Sambungkan ke PC melalui mod EDL (Qualcomm 9008) menggunakan Mi Flash Tool dan Stock Fastboot ROM.
                
                Pautkan ke dokumentasi rasmi: https://docs.blissos.org
            """.trimIndent()

            else -> """
                🤖 **Bantuan Teknikal Bliss OS Redmi 9T:**
                Terima kasih atas pertanyaan anda tentang "$prompt".
                
                Untuk peranti Redmi 9T (Snapdragon 662), Bliss OS menawarkan prestasi yang lancar, antaramuka moden, dan jangka hayat bateri yang cemerlang.
                
                📌 Langkah utama:
                1. Pastikan Bootloader sudah di-unlock.
                2. Sediakan fail ROM di MicroSD atau gunakan skrip Fastboot Windows.
                3. Lakukan sandaran (Backup) data EFS dan partition utama.
                4. Flash ROM dan lakukan format data (F2FS / EXT4).
                
                Dokumentasi Rasmi: https://docs.blissos.org | Bantuan: RazifAppStudios@nasadef
            """.trimIndent()
        }
    }
}
