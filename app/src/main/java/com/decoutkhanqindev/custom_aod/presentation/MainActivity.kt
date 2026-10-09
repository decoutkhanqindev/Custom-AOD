package com.decoutkhanqindev.custom_aod.presentation

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.decoutkhanqindev.custom_aod.ads.AdsManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.aod.AodService
import com.decoutkhanqindev.custom_aod.presentation.components.AppLanguageProvider
import com.decoutkhanqindev.custom_aod.presentation.navigation.AppNavDisplay
import com.decoutkhanqindev.custom_aod.presentation.theme.AodsTheme
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val dataStoreManager: DataStoreManager by inject()
    private val adsManager: AdsManager by inject()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        ComposeUiFlags.isBypassUnfocusableComposeViewEnabled = false
        super.onCreate(savedInstanceState)
        adsManager.requestConsent(this)
        runCatching {  requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        // AodsTheme luôn tối: icon thanh hệ thống luôn sáng, không theo theme hệ thống.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        startAodServiceIfEnabled()
        setContent {
            AppLanguageProvider {
                AodsTheme {
                    AppNavDisplay(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    // Mở app là cách chạy lại service nếu hệ thống (vd Xiaomi) đã đóng nó; khi khởi động máy thì BootReceiver lo.
    private fun startAodServiceIfEnabled() {
        lifecycleScope.launch {
            if (dataStoreManager.isAodEnabled.filterNotNull().first()) AodService.start(this@MainActivity)
        }
    }
}
