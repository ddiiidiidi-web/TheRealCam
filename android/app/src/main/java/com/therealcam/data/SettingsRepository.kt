package com.therealcam.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** Bump when the list of collected data changes so the consent screen is shown again. */
const val DATASET_CONSENT_VERSION = 1

data class AppSettings(
    val datasetMode: Boolean = false,
    val consentVersion: Int = 0,
    val geotag: Boolean = true,
    val askRating: Boolean = true,
    val saveToGallery: Boolean = true,
    val datasetImageMaxSide: Int = 1600,
    val jpegQuality: Int = 95,
    val grid: Boolean = true,
    val level: Boolean = true,
    val volumeShutter: Boolean = true,
    val timerSeconds: Int = 0,
    val activeModel: String? = null,
    val lastMode: String = "auto",
    val lastCategoryHint: String? = null,
    /** VIDEO mode: VideoQuality key, frame rate and stabilization wish (applied when the camera supports it). */
    val videoQuality: String = "1080p",
    val videoFps: Int = 30,
    val videoStabilization: Boolean = true,
    /** The first-run screen (own dataset or a shared model) was completed or skipped. */
    val onboarded: Boolean = false,
    /** Astro mode: total shooting time of one picture. */
    val astroDurationS: Int = 240,
    /** Voice countdown of long series every 30 s. */
    val voice: Boolean = true,
)

class SettingsRepository(private val context: Context) {
    private object K {
        val datasetMode = booleanPreferencesKey("dataset_mode")
        val consentVersion = intPreferencesKey("consent_version")
        val geotag = booleanPreferencesKey("geotag")
        val askRating = booleanPreferencesKey("ask_rating")
        val saveToGallery = booleanPreferencesKey("save_to_gallery")
        val datasetImageMaxSide = intPreferencesKey("dataset_image_max_side")
        val jpegQuality = intPreferencesKey("jpeg_quality")
        val grid = booleanPreferencesKey("grid")
        val level = booleanPreferencesKey("level")
        val volumeShutter = booleanPreferencesKey("volume_shutter")
        val timerSeconds = intPreferencesKey("timer_seconds")
        val activeModel = stringPreferencesKey("active_model")
        val lastMode = stringPreferencesKey("last_mode")
        val lastCategoryHint = stringPreferencesKey("last_category_hint")
        val videoQuality = stringPreferencesKey("video_quality")
        val videoFps = intPreferencesKey("video_fps")
        val videoStabilization = booleanPreferencesKey("video_stabilization")
        val onboarded = booleanPreferencesKey("onboarded")
        val astroDurationS = intPreferencesKey("astro_duration_s")
        val voice = booleanPreferencesKey("voice")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toSettings() }

    suspend fun current(): AppSettings = settings.first()

    private fun Preferences.toSettings(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            datasetMode = this[K.datasetMode] ?: d.datasetMode,
            consentVersion = this[K.consentVersion] ?: d.consentVersion,
            geotag = this[K.geotag] ?: d.geotag,
            askRating = this[K.askRating] ?: d.askRating,
            saveToGallery = this[K.saveToGallery] ?: d.saveToGallery,
            datasetImageMaxSide = this[K.datasetImageMaxSide] ?: d.datasetImageMaxSide,
            jpegQuality = this[K.jpegQuality] ?: d.jpegQuality,
            grid = this[K.grid] ?: d.grid,
            level = this[K.level] ?: d.level,
            volumeShutter = this[K.volumeShutter] ?: d.volumeShutter,
            timerSeconds = this[K.timerSeconds] ?: d.timerSeconds,
            activeModel = this[K.activeModel],
            lastMode = this[K.lastMode] ?: d.lastMode,
            lastCategoryHint = this[K.lastCategoryHint],
            videoQuality = this[K.videoQuality] ?: d.videoQuality,
            videoFps = this[K.videoFps] ?: d.videoFps,
            videoStabilization = this[K.videoStabilization] ?: d.videoStabilization,
            onboarded = this[K.onboarded] ?: d.onboarded,
            astroDurationS = this[K.astroDurationS] ?: d.astroDurationS,
            voice = this[K.voice] ?: d.voice,
        )
    }

    suspend fun setDatasetMode(enabled: Boolean) = context.dataStore.edit {
        it[K.datasetMode] = enabled
        if (enabled) it[K.consentVersion] = DATASET_CONSENT_VERSION
    }

    suspend fun setGeotag(v: Boolean) = context.dataStore.edit { it[K.geotag] = v }
    suspend fun setAskRating(v: Boolean) = context.dataStore.edit { it[K.askRating] = v }
    suspend fun setSaveToGallery(v: Boolean) = context.dataStore.edit { it[K.saveToGallery] = v }
    suspend fun setDatasetImageMaxSide(v: Int) = context.dataStore.edit { it[K.datasetImageMaxSide] = v }
    suspend fun setJpegQuality(v: Int) = context.dataStore.edit { it[K.jpegQuality] = v.coerceIn(70, 100) }
    suspend fun setGrid(v: Boolean) = context.dataStore.edit { it[K.grid] = v }
    suspend fun setLevel(v: Boolean) = context.dataStore.edit { it[K.level] = v }
    suspend fun setVolumeShutter(v: Boolean) = context.dataStore.edit { it[K.volumeShutter] = v }
    suspend fun setTimerSeconds(v: Int) = context.dataStore.edit { it[K.timerSeconds] = v }
    suspend fun setVideo(quality: String, fps: Int, stabilization: Boolean) = context.dataStore.edit {
        it[K.videoQuality] = quality
        it[K.videoFps] = fps
        it[K.videoStabilization] = stabilization
    }
    suspend fun setLastMode(v: String) = context.dataStore.edit { it[K.lastMode] = v }
    suspend fun setOnboarded(v: Boolean) = context.dataStore.edit { it[K.onboarded] = v }
    suspend fun setAstroDurationS(v: Int) = context.dataStore.edit { it[K.astroDurationS] = v.coerceIn(10, 3600) }
    suspend fun setVoice(v: Boolean) = context.dataStore.edit { it[K.voice] = v }

    suspend fun setActiveModel(fileName: String?) = context.dataStore.edit {
        if (fileName == null) it.remove(K.activeModel) else it[K.activeModel] = fileName
    }

    suspend fun setLastCategoryHint(id: String?) = context.dataStore.edit {
        if (id == null) it.remove(K.lastCategoryHint) else it[K.lastCategoryHint] = id
    }
}
