package com.decoutkhanqindev.custom_aod.presentation.aod

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.MainActivity
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class AodTileService : TileService(), Tag {

    private val dataStoreManager: DataStoreManager by inject()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var listeningJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        listeningJob?.cancel()
        listeningJob = scope.launch {
            dataStoreManager.isAodEnabled.filterNotNull().collectCatching(
                action = { isEnabled -> renderTile(isEnabled) },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    override fun onStopListening() {
        listeningJob?.cancel()
        super.onStopListening()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()
        val isEnabled = !(dataStoreManager.isAodEnabled.value ?: return)
        dataStoreManager.saveIsAodEnabled(isEnabled)
        renderTile(isEnabled)
        when {
            !isEnabled -> AodService.stop(this)
            // Từ Android 15, app có "Hiển thị trên ứng dụng khác" chỉ được start foreground service từ nền khi đang có overlay: mở app để MainActivity start.
            !AodService.start(this) -> openApp()
        }
    }

    private fun renderTile(isEnabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val start = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(this, OPEN_APP_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE),
                )
            } else {
                // Bản nhận Intent chỉ ném lỗi từ Android 14, còn bản nhận PendingIntent thì chưa có dưới API 34.
                @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
                startActivityAndCollapse(intent)
            }
        }
        if (isLocked) unlockAndRun { start() } else start()
    }

    companion object {
        private const val OPEN_APP_REQUEST_CODE = 2
    }
}
