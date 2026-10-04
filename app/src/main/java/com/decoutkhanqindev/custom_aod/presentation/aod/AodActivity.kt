package com.decoutkhanqindev.custom_aod.presentation.aod

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.components.AppLanguageProvider
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.AodScreen
import org.koin.android.ext.android.inject

class AodActivity : ComponentActivity() {

    private val dataStoreManager: DataStoreManager by inject()
    private val session: AodSession by inject()

    private var isPreview = false
    private var normalBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    private var isDark = false

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        ComposeUiFlags.isBypassUnfocusableComposeViewEnabled = false
        super.onCreate(savedInstanceState)
        // Cờ cửa sổ, không dùng android:turnScreenOn/setTurnScreenOn: màn hình chỉ sáng khi cửa sổ đã che màn hình khoá, nên màn hình khoá không loé lên và nhận diện khuôn mặt không chạy.
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
        )

        isPreview = intent.getBooleanExtra(EXTRA_PREVIEW, false)
        val isCustomBrightness =
            dataStoreManager.isAodCustomBrightness.value ?: DataStoreManager.DEFAULT_IS_AOD_CUSTOM_BRIGHTNESS
        val brightnessPercent =
            dataStoreManager.aodBrightnessPercent.value ?: DataStoreManager.DEFAULT_AOD_BRIGHTNESS_PERCENT
        normalBrightness = if (isCustomBrightness) {
            brightnessPercent / MAX_BRIGHTNESS_PERCENT
        } else {
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        }
        // Bản xem thử không phải AOD: service không được coi nó là AOD khi màn hình tắt.
        if (!isPreview) session.attach(this)

        // Độ sáng đặt trước khi cửa sổ hiện để ngay khung đầu tiên đã đúng mức.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setBrightness(normalBrightness)
        setContent {
            AppLanguageProvider {
                AodScreen(
                    isPreview = isPreview,
                    onDarkChange = ::renderDark,
                    onClose = ::close,
                )
            }
        }
        hideSystemBars()
    }

    override fun onStop() {
        super.onStop()
        // AOD thật có thể bị stop khi màn hình vẫn tắt nên service quyết định lúc đóng; chỉ bản xem thử đóng ở đây.
        if (isPreview) close()
    }

    override fun onDestroy() {
        session.detach(this)
        super.onDestroy()
    }

    fun close() {
        if (isFinishing) return
        finish()
        @Suppress("DEPRECATION") // overridePendingTransition: hàm thay thế chỉ có từ API 34
        overridePendingTransition(0, 0)
    }

    // App không tắt được màn hình: đen ở độ sáng thấp nhất, bỏ KEEP_SCREEN_ON để giờ chờ của máy tắt; lần tắt đó là chủ ý nên service không mở lại AOD.
    private fun renderDark(isDark: Boolean) {
        if (isDark == this.isDark) return
        this.isDark = isDark
        if (isDark) {
            session.requestSleep()
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF)
        } else {
            session.clearSleepRequest()
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setBrightness(normalBrightness)
        }
    }

    private fun setBrightness(value: Float) {
        window.attributes = window.attributes.apply { screenBrightness = value }
    }

    @Suppress("DEPRECATION") // setDecorFitsSystemWindows: từ API 35 luôn edge-to-edge
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setDecorFitsSystemWindows(false)
        }
        window.insetsController?.apply {
            hide(WindowInsets.Type.systemBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    companion object {
        private const val EXTRA_PREVIEW = "com.decoutkhanqindev.custom_aod.extra.PREVIEW"
        private const val MAX_BRIGHTNESS_PERCENT = 100f

        fun preview(context: Context) {
            context.startActivity(Intent(context, AodActivity::class.java).putExtra(EXTRA_PREVIEW, true))
        }
    }
}
