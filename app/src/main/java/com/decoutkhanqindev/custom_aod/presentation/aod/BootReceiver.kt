package com.decoutkhanqindev.custom_aod.presentation.aod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// Hai broadcast này được miễn lệnh cấm start foreground service từ nền của Android 12. Receiver do hệ thống tạo nên lấy dependency qua KoinComponent.
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val dataStoreManager: DataStoreManager by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }
        // DataStore đọc bất đồng bộ: giữ broadcast sống bằng goAsync() tới khi đọc xong.
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                if (dataStoreManager.isAodEnabled.filterNotNull().first()) AodService.start(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
