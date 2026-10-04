package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.audio.AudioStateManager
import com.decoutkhanqindev.custom_aod.data.device.battery.BatteryStateManager
import com.decoutkhanqindev.custom_aod.data.device.proximity.ProximityManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.AodScheduleUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.currentAodRules
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
) : BaseViewModel<AodState, AodIntent, AodEffect>(
    initialState = initialState(batteryStateManager),
), Tag {

    private val isProximityEnabled =
        dataStoreManager.isAodProximityEnabled.value ?: DataStoreManager.DEFAULT_IS_AOD_PROXIMITY_ENABLED
    private val timeoutMinutes =
        dataStoreManager.aodTimeoutMinutes.value ?: DataStoreManager.DEFAULT_AOD_TIMEOUT_MINUTES
    private val rules = dataStoreManager.currentAodRules()

    // Tối vì bị che (khác với hết giờ, pin yếu, ngoài quy tắc): chỉ trường hợp này mới sáng lại khi lấy máy ra.
    private var isDarkUntilUncovered = false
    private var isCovered = false
    private var isPlugged = batteryStateManager.readIsPlugged()
    private var timeoutJob: Job? = null
    private var hintJob: Job? = null

    init {
        observeBattery()
        observePlugged()
        observeAudio()
        startMinuteTicks()
        scheduleHintHide()
        if (!isPreview) {
            if (isProximityEnabled) observeProximity()
            armTimeout()
        }
    }

    override fun onIntent(intent: AodIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        when (intent) {
            // Trong túi, vải cọ lên màn hình không được mở màn hình khoá.
            is AodIntent.DoubleTap -> if (!proximityManager.isNear.value) requestClose()
        }
    }

    private fun requestClose() {
        viewModelScope.launch { sendEffect(AodEffect.Close) }
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
                    action = { requestClose() },
                    catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
                )
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
        updateState { copy(isDark = true) }
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
        private const val MAX_SHIFT_X_DP = 24
        private const val MAX_SHIFT_Y_DP = 64

        // Pin đọc đồng bộ từ broadcast sticky để khung đầu tiên đã có dòng pin, bố cục không bị nhảy.
        private fun initialState(batteryStateManager: BatteryStateManager): AodState = AodState(
            nowMillis = System.currentTimeMillis(),
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
