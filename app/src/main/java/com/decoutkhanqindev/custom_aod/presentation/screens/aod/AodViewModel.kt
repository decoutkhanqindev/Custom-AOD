package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.annotation.ColorInt
import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.audio.AudioStateManager
import com.decoutkhanqindev.custom_aod.data.device.battery.BatteryStateManager
import com.decoutkhanqindev.custom_aod.data.device.media.MediaStateManager
import com.decoutkhanqindev.custom_aod.data.device.notification.NotificationStateManager
import com.decoutkhanqindev.custom_aod.data.device.proximity.ProximityManager
import com.decoutkhanqindev.custom_aod.data.local.background.BackgroundImageManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodScheduleUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.currentAodAppearance
import com.decoutkhanqindev.custom_aod.presentation.model.currentAodNotificationOptions
import com.decoutkhanqindev.custom_aod.presentation.model.currentAodRules
import com.decoutkhanqindev.custom_aod.presentation.model.glowColorArgb
import com.decoutkhanqindev.custom_aod.presentation.model.toAodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.toUiModel
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.utils.Tag
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

class AodViewModel(
    private val isPreview: Boolean,
    dataStoreManager: DataStoreManager,
    private val batteryStateManager: BatteryStateManager,
    private val audioStateManager: AudioStateManager,
    private val proximityManager: ProximityManager,
    private val notificationStateManager: NotificationStateManager,
    private val mediaStateManager: MediaStateManager,
    private val backgroundImageManager: BackgroundImageManager,
) : BaseViewModel<AodState, AodIntent, AodEffect>(
    initialState = initialState(dataStoreManager, batteryStateManager),
), Tag {

    private val isProximityEnabled =
        dataStoreManager.isAodProximityEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_PROXIMITY_ENABLED
    private val timeoutMinutes =
        dataStoreManager.aodTimeoutMinutes.value ?: DataStoreManager.DEFAULT_AOD_TIMEOUT_MINUTES
    private val rules = dataStoreManager.currentAodRules()
    private val notificationOptions = dataStoreManager.currentAodNotificationOptions()

    // Tối vì bị che (khác với hết giờ, pin yếu, ngoài quy tắc): chỉ trường hợp này mới sáng lại khi lấy máy ra.
    private var isDarkUntilUncovered = false
    private var isCovered = false
    private var isPlugged = batteryStateManager.readIsPlugged()
    private var timeoutJob: Job? = null
    private var hintJob: Job? = null
    private var glowJob: Job? = null

    init {
        observeBattery()
        observePlugged()
        observeAudio()
        // StateFlow nóng và viewModelScope chạy ngay trên main thread: icon và nhạc đã vào state trước khung đầu tiên.
        if (notificationOptions.isIconsEnabled) observeNotifications()
        if (notificationOptions.isEdgeGlowEnabled) observeAlerts()
        if (notificationOptions.isMediaControlsEnabled) observeMedia()
        loadBackground()
        startMinuteTicks()
        scheduleHintHide()
        if (!isPreview) {
            if (isProximityEnabled) observeProximity()
            armTimeout()
        }
    }

    override fun onIntent(intent: AodIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        // Trong túi, vải cọ lên màn hình không được mở màn hình khoá hay bấm nút nhạc.
        if (proximityManager.isNear.value) return
        when (intent) {
            is AodIntent.DoubleTap -> closeAod()
            is AodIntent.PlayPauseMedia -> mediaStateManager.playPause()
            is AodIntent.SkipToPreviousTrack -> mediaStateManager.skipToPrevious()
            is AodIntent.SkipToNextTrack -> mediaStateManager.skipToNext()
        }
    }

    private fun closeAod() {
        viewModelScope.launch { sendEffect(AodEffect.CloseAod) }
    }

    private fun observeBattery() {
        viewModelScope.launch {
            combine(
                batteryStateManager.levelPercent.filterNotNull(),
                batteryStateManager.isCharging.filterNotNull(),
            ) { percent, isCharging -> BatteryUiModel(percent = percent, isCharging = isCharging) }
                .collectCatching(
                    action = { battery ->
                        updateState { copy(battery = battery) }
                        checkRules()
                    },
                    catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
                )
        }
    }

    private fun observePlugged() {
        viewModelScope.launch {
            batteryStateManager.isPlugged.filterNotNull().collectCatching(
                action = { isPlugged ->
                    this@AodViewModel.isPlugged = isPlugged
                    checkRules()
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Màn hình cuộc gọi, báo thức mở lên trên thì noHistory cũng đóng AOD; đóng ngay khi có dấu hiệu chỉ làm việc đó sớm hơn.
    private fun observeAudio() {
        viewModelScope.launch {
            audioStateManager.isBusy
                .filterNotNull()
                .drop(1)
                .filter { isBusy -> isBusy }
                .collectCatching(
                    action = { closeAod() },
                    catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
                )
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationStateManager.notifications.collectCatching(
                action = { notifications ->
                    updateState { copy(notifications = notifications.toAodNotificationsUiModel()) }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Đang tối thì không sáng viền: màn hình đen đang chờ giờ chờ của máy tắt.
    private fun observeAlerts() {
        viewModelScope.launch {
            notificationStateManager.alerts.collectCatching(
                action = { notification -> if (!state.value.isDark) glow(notification.glowColorArgb()) },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeMedia() {
        viewModelScope.launch {
            mediaStateManager.playback.collectCatching(
                action = { playback -> updateState { copy(media = playback?.toUiModel()) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Giải mã ảnh nền tốn vài chục ms: đồng hồ hiện trước, ảnh nền hiện dần sau, không chặn khung đầu tiên.
    private fun loadBackground() {
        viewModelScope.launch {
            val background = backgroundImageManager.loadImage() ?: return@launch
            updateState { copy(background = background) }
        }
    }

    private fun startMinuteTicks() {
        viewModelScope.launch {
            while (isActive) {
                delay(MINUTE_MILLIS - System.currentTimeMillis() % MINUTE_MILLIS + TICK_SLACK_MILLIS)
                onMinuteTick()
            }
        }
    }

    // Phải bị che liên tục COVER_DELAY_MILLIS (trong túi, úp mặt); bàn tay lướt qua ngắn hơn nên bị bỏ qua.
    private fun observeProximity() {
        viewModelScope.launch {
            proximityManager.isNear.collectLatest { isNear ->
                if (isNear) {
                    if (isCovered) return@collectLatest
                    delay(COVER_DELAY_MILLIS)
                    isCovered = true
                    goDark(isUntilUncovered = true)
                } else if (isCovered) {
                    isCovered = false
                    lightUpAgain()
                }
            }
        }
    }

    private fun armTimeout() {
        timeoutJob?.cancel()
        if (timeoutMinutes > 0) {
            timeoutJob = viewModelScope.launch {
                delay(timeoutMinutes * MINUTE_MILLIS)
                goDark()
            }
        }
    }

    private fun glow(@ColorInt colorArgb: Int?) {
        glowJob?.cancel()
        updateState { copy(isGlowing = true, glowColorArgb = colorArgb) }
        glowJob = viewModelScope.launch {
            delay(GLOW_MILLIS)
            updateState { copy(isGlowing = false) }
        }
    }

    private fun scheduleHintHide() {
        hintJob?.cancel()
        hintJob = viewModelScope.launch {
            delay(HINT_VISIBLE_MILLIS)
            updateState { copy(isHintVisible = false) }
        }
    }

    private fun onMinuteTick() {
        val nowMillis = System.currentTimeMillis()
        updateState {
            if (isDark) {
                copy(nowMillis = nowMillis)
            } else {
                copy(nowMillis = nowMillis, shiftXDp = randomShiftX(), shiftYDp = randomShiftY())
            }
        }
        checkRules()
    }

    // Hết khung giờ, rút / cắm sạc trái quy tắc hoặc pin yếu khi đồng hồ đang hiện: chuyển đen như khi hết giờ.
    private fun checkRules() {
        if (isPreview) return
        val battery = state.value.battery
        val isAllowed = rules.allows(
            batteryPercent = battery?.percent,
            isCharging = battery?.isCharging == true,
            isPlugged = isPlugged,
            minuteOfDay = AodScheduleUiModel.minuteOfDay(state.value.nowMillis),
        )
        if (!isAllowed) goDark()
    }

    private fun goDark(isUntilUncovered: Boolean = false) {
        if (state.value.isDark) return
        isDarkUntilUncovered = isUntilUncovered
        timeoutJob?.cancel()
        glowJob?.cancel()
        updateState { copy(isDark = true, isGlowing = false) }
    }

    // Lấy máy khỏi túi trước khi màn hình hết giờ chờ: hiện lại đồng hồ.
    private fun lightUpAgain() {
        if (!state.value.isDark || !isDarkUntilUncovered) return
        isDarkUntilUncovered = false
        updateState { copy(isDark = false, isHintVisible = true) }
        scheduleHintHide()
        armTimeout()
    }

    companion object {
        private const val MINUTE_MILLIS = 60_000L
        private const val TICK_SLACK_MILLIS = 50L
        private const val COVER_DELAY_MILLIS = 3_000L
        private const val HINT_VISIBLE_MILLIS = 3_000L
        private const val GLOW_MILLIS = 4_000L
        private const val MAX_SHIFT_X_DP = 24
        private const val MAX_SHIFT_Y_DP = 64

        // Pin đọc đồng bộ từ broadcast sticky để khung đầu tiên đã có dòng pin, bố cục không bị nhảy; giao diện đồng hồ cũng có ngay từ khung đầu.
        private fun initialState(
            dataStoreManager: DataStoreManager,
            batteryStateManager: BatteryStateManager,
        ): AodState = AodState(
            nowMillis = System.currentTimeMillis(),
            appearance = dataStoreManager.currentAodAppearance(),
            battery = batteryStateManager.readLevelPercent()?.let { percent ->
                BatteryUiModel(percent = percent, isCharging = batteryStateManager.readIsCharging() == true)
            },
            shiftXDp = randomShiftX(),
            shiftYDp = randomShiftY(),
        )

        // Chống burn-in: mỗi phút nội dung sang một vị trí ngẫu nhiên.
        private fun randomShiftX(): Int = Random.nextInt(-MAX_SHIFT_X_DP, MAX_SHIFT_X_DP + 1)

        private fun randomShiftY(): Int = Random.nextInt(-MAX_SHIFT_Y_DP, MAX_SHIFT_Y_DP + 1)
    }
}
