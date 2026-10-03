package com.decoutkhanqindev.custom_aod.data.device.audio

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import androidx.annotation.RequiresApi
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.recoverCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

class AudioStateManager(
    private val app: Application,
) : Tag {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val audioManager = app.getSystemService(AudioManager::class.java)

    // true khi đang có chuông, cuộc gọi hoặc báo thức: màn hình của chúng phải nằm trên AOD.
    val isBusy: StateFlow<Boolean?> = callbackFlow {
        val playbackCallback = object : AudioManager.AudioPlaybackCallback() {
            override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
                trySend(isBusyNow())
            }
        }
        audioManager.registerAudioPlaybackCallback(playbackCallback, null)
        val modeWatcher = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ModeWatcher(audioManager) { trySend(isBusyNow()) }.also { it.start(app) }
        } else {
            null
        }
        trySend(isBusyNow())
        // Dưới Android 12 không có listener cho mode: hỏi lại mỗi phút.
        if (modeWatcher == null) {
            launch {
                while (isActive) {
                    delay(MODE_POLL_MILLIS)
                    trySend(isBusyNow())
                }
            }
        }
        awaitClose {
            audioManager.unregisterAudioPlaybackCallback(playbackCallback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) modeWatcher?.stop()
        }
    }
        .distinctUntilChanged()
        .recoverCatching { throwable ->
            Timber.tag(tag).e("isBusy have error: ${throwable.stackTraceToString()}")
            emit(false)
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            initialValue = null,
        )

    fun isBusyNow(): Boolean =
        audioManager.mode != AudioManager.MODE_NORMAL ||
            audioManager.activePlaybackConfigurations.any { it.audioAttributes.usage in URGENT_USAGES }

    // Class riêng để OnModeChangedListener (API 31) không bao giờ được nạp trên máy cũ hơn.
    @RequiresApi(Build.VERSION_CODES.S)
    private class ModeWatcher(
        private val audioManager: AudioManager,
        private val onModeChanged: () -> Unit,
    ) {
        private val listener = AudioManager.OnModeChangedListener { onModeChanged() }

        fun start(context: Context) = audioManager.addOnModeChangedListener(context.mainExecutor, listener)

        fun stop() = audioManager.removeOnModeChangedListener(listener)
    }

    companion object {
        private const val MODE_POLL_MILLIS = 60_000L
        private val URGENT_USAGES = setOf(
            AudioAttributes.USAGE_ALARM,
            AudioAttributes.USAGE_NOTIFICATION_RINGTONE,
            AudioAttributes.USAGE_VOICE_COMMUNICATION,
            AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING,
        )
    }
}
