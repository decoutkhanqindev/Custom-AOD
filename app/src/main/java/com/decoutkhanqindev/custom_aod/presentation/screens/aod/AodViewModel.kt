package com.decoutkhanqindev.custom_aod.presentation.screens.aod

import androidx.annotation.ColorInt
import androidx.lifecycle.viewModelScope
import com.decoutkhanqindev.custom_aod.data.device.audio.AudioStateManager
import com.decoutkhanqindev.custom_aod.data.device.battery.BatteryStateManager
import com.decoutkhanqindev.custom_aod.data.device.flashlight.FlashlightManager
import com.decoutkhanqindev.custom_aod.data.device.light.AmbientLightManager
import com.decoutkhanqindev.custom_aod.data.device.media.MediaStateManager
import com.decoutkhanqindev.custom_aod.data.device.notification.NotificationStateManager
import com.decoutkhanqindev.custom_aod.data.device.proximity.ProximityManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.image.AodImageManager
import com.decoutkhanqindev.custom_aod.domain.usecase.GetUpcomingEventsUseCase
import com.decoutkhanqindev.custom_aod.domain.usecase.ObserveWeatherUseCase
import com.decoutkhanqindev.custom_aod.domain.usecase.RefreshWeatherUseCase
import com.decoutkhanqindev.custom_aod.presentation.base.BaseViewModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.WallpaperValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.appearance.currentAodAppearance
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.BatteryUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.CalendarEventUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.glowColorArgb
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.toAodNotificationsUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.info.toUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodActionValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodGestureValue
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.AodScheduleUiModel
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.currentAodExtras
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.currentAodInteraction
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.currentAodNotificationOptions
import com.decoutkhanqindev.custom_aod.presentation.model.aod.settings.currentAodRules
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodEffect
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodIntent
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.state.AodState
import com.decoutkhanqindev.custom_aod.utils.collectCatching
import com.decoutkhanqindev.custom_aod.utils.collectLatestCatching
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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
    private val aodImageManager: AodImageManager,
    private val flashlightManager: FlashlightManager,
    private val ambientLightManager: AmbientLightManager,
    private val observeWeatherUseCase: ObserveWeatherUseCase,
    private val refreshWeatherUseCase: RefreshWeatherUseCase,
    private val getUpcomingEventsUseCase: GetUpcomingEventsUseCase,
) : BaseViewModel<AodState, AodIntent, AodEffect>(
    initialState = initialState(dataStoreManager, batteryStateManager),
) {

    private val isProximityEnabled =
        dataStoreManager.isAodProximityEnabled.value
            ?: DataStoreManager.DEFAULT_IS_AOD_PROXIMITY_ENABLED
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
    private var nextEventsReloadMillis = 0L
    private var nextWeatherRefreshMillis = 0L

    init {
        observeBattery()
        observePlugged()
        observeAudio()
        // StateFlow nóng và viewModelScope chạy ngay trên main thread: icon và nhạc đã vào state trước khung đầu tiên.
        if (notificationOptions.isIconsEnabled || notificationOptions.isContentEnabled) observeNotifications()
        if (notificationOptions.isEdgeGlowEnabled) observeAlerts()
        if (notificationOptions.isMediaControlsEnabled) observeMedia()
        loadBackground()
        loadDrawing()
        if (state.value.extras.isWeatherEnabled) {
            observeWeather()
            refreshWeather()
        }
        if (state.value.extras.isCalendarEnabled) loadEvents()
        if (flashlightManager.isAvailable) observeFlashlight()
        if (state.value.interaction.isAutoDimEnabled && ambientLightManager.isAvailable) observeAmbientLight()
        startMinuteTicks()
        scheduleHintHide()
        if (!isPreview) {
            if (isProximityEnabled) observeProximity()
            armTimeout()
        }
    }

    override fun onIntent(intent: AodIntent) {
        Timber.tag(tag).d("onIntent: $intent")
        // Trong túi, vải cọ lên màn hình hay phím bị đè không được mở màn hình khoá, bật đèn pin hay bấm nút nhạc.
        if (proximityManager.isNear.value) return
        when (intent) {
            is AodIntent.PerformGesture -> performGesture(intent.gesture)
            is AodIntent.PlayPauseMedia -> mediaStateManager.playPause()
            is AodIntent.SkipToPreviousTrack -> mediaStateManager.skipToPrevious()
            is AodIntent.SkipToNextTrack -> mediaStateManager.skipToNext()
        }
    }

    private fun performGesture(gesture: AodGestureValue) {
        when (state.value.interaction.actionOf(gesture)) {
            AodActionValue.NONE -> Unit
            AodActionValue.CLOSE -> closeAod()
            AodActionValue.GO_DARK -> goDark()
            AodActionValue.FLASHLIGHT -> flashlightManager.toggle()
            AodActionValue.PLAY_PAUSE -> mediaStateManager.playPause()
            AodActionValue.PREVIOUS_TRACK -> mediaStateManager.skipToPrevious()
            AodActionValue.NEXT_TRACK -> mediaStateManager.skipToNext()
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
                    block = { battery ->
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
                block = { isPlugged ->
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
                    block = { closeAod() },
                    catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
                )
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            notificationStateManager.notifications.collectCatching(
                block = { notifications ->
                    updateState {
                        copy(
                            notifications = notifications.toAodNotificationsUiModel(
                                notificationOptions
                            )
                        )
                    }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Đang tối thì không sáng viền: màn hình đen đang chờ giờ chờ của máy tắt.
    private fun observeAlerts() {
        viewModelScope.launch {
            notificationStateManager.alerts.collectCatching(
                block = { notification -> if (!state.value.isDark) glow(notification.glowColorArgb()) },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeMedia() {
        viewModelScope.launch {
            mediaStateManager.playback.collectCatching(
                block = { playback -> updateState { copy(media = playback?.toUiModel()) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    private fun observeFlashlight() {
        viewModelScope.launch {
            flashlightManager.isOn.collectCatching(
                block = { isOn -> updateState { copy(isFlashlightOn = isOn) } },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Phòng tối thì giảm về mức thấp nhất. Ngưỡng tối và sáng lệch nhau, mức mới phải giữ LIGHT_SETTLE_MILLIS mới đổi: bóng tay hay đèn chớp không làm màn hình nhấp nháy.
    private fun observeAmbientLight() {
        viewModelScope.launch {
            ambientLightManager.lux
                .filterNotNull()
                .map { lux ->
                    when {
                        lux <= DIM_LUX -> true
                        lux >= UNDIM_LUX -> false
                        else -> null
                    }
                }
                .distinctUntilChanged()
                .collectLatestCatching(
                    block = { isDarkRoom ->
                        if (isDarkRoom == null || isDarkRoom == state.value.isDimmed) return@collectLatestCatching
                        delay(LIGHT_SETTLE_MILLIS)
                        updateState { copy(isDimmed = isDarkRoom) }
                    },
                    catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
                )
        }
    }

    // Giải mã ảnh nền tốn vài chục ms: đồng hồ hiện trước, ảnh nền hiện dần sau, không chặn khung đầu tiên. Đang dùng ảnh có sẵn thì không đọc file.
    private fun loadBackground() {
        if (state.value.wallpaper != null) return
        viewModelScope.launch {
            val background = aodImageManager.loadBackground() ?: return@launch
            updateState { copy(background = background) }
        }
    }

    private fun loadDrawing() {
        viewModelScope.launch {
            val drawing = aodImageManager.loadDrawing() ?: return@launch
            updateState { copy(drawing = drawing) }
        }
    }

    private fun observeWeather() {
        viewModelScope.launch {
            observeWeatherUseCase().collectCatching(
                block = { savedWeather ->
                    val nowMillis = System.currentTimeMillis()
                    updateState {
                        copy(
                            weather = savedWeather
                                ?.toUiModel(isFahrenheit = extras.isWeatherFahrenheit)
                                ?.takeIf { it.isFreshAt(nowMillis) },
                        )
                    }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
        }
    }

    // Bản đã lưu hiện ngay, bản mới tải ngầm lúc AOD hiện (app đang ở tiền cảnh nên được dùng vị trí); lỗi thì giữ bản cũ.
    private fun refreshWeather() {
        val nowMillis = System.currentTimeMillis()
        nextWeatherRefreshMillis = nowMillis + WEATHER_RETRY_MILLIS
        viewModelScope.launch {
            refreshWeatherUseCase(nowMillis = nowMillis)
                .onFailure { e -> Timber.tag(tag).w("Could not refresh the weather: ${e.message}") }
        }
    }

    // AOD có thể hiện liền nhiều giờ (đồng hồ đêm lúc sạc): thử tải lại mỗi WEATHER_RETRY_MILLIS (UseCase chỉ gọi mạng khi bản đã lưu đủ cũ), bản quá cũ thì rời đồng hồ.
    private fun updateWeather(nowMillis: Long) {
        if (nowMillis >= nextWeatherRefreshMillis) refreshWeather()
        updateState { copy(weather = weather?.takeIf { it.isFreshAt(nowMillis) }) }
    }

    // Đọc lại mỗi EVENTS_RELOAD_MILLIS để có sự kiện mới thêm; giữa hai lần đọc, sự kiện đã kết thúc rời đồng hồ ở tick mỗi phút.
    private fun loadEvents() {
        val nowMillis = System.currentTimeMillis()
        nextEventsReloadMillis = nowMillis + EVENTS_RELOAD_MILLIS
        viewModelScope.launch {
            getUpcomingEventsUseCase(
                nowMillis = nowMillis,
                endOfDayMillis = CalendarEventUiModel.endOfDayMillis(nowMillis),
                limit = CalendarEventUiModel.MAX_EVENTS,
            )
                .onSuccess { events ->
                    updateState {
                        copy(events = events.map { it.toUiModel() }.toImmutableList())
                    }
                }
                .onFailure { e ->
                    Timber.tag(tag).w("Could not load calendar events: ${e.message}")
                }
        }
    }

    private fun updateEvents(nowMillis: Long) {
        if (nowMillis >= nextEventsReloadMillis) {
            loadEvents()
        } else {
            updateState {
                copy(events = events.filter { event -> event.endMillis > nowMillis }
                    .toImmutableList())
            }
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
            proximityManager.isNear.collectLatestCatching(
                block = { isNear ->
                    if (isNear) {
                        if (isCovered) return@collectLatestCatching
                        delay(COVER_DELAY_MILLIS)
                        isCovered = true
                        goDark(isUntilUncovered = true)
                    } else if (isCovered) {
                        isCovered = false
                        lightUpAgain()
                    }
                },
                catch = { e -> Timber.tag(tag).e(e.stackTraceToString()) },
            )
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
        if (state.value.extras.isWeatherEnabled) updateWeather(nowMillis)
        if (state.value.extras.isCalendarEnabled) updateEvents(nowMillis)
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
        private const val LIGHT_SETTLE_MILLIS = 2_000L
        private const val DIM_LUX = 5f
        private const val UNDIM_LUX = 20f
        private const val EVENTS_RELOAD_MILLIS = 15 * 60_000L
        private const val WEATHER_RETRY_MILLIS = 10 * 60_000L
        private const val MAX_SHIFT_X_DP = 24
        private const val MAX_SHIFT_Y_DP = 64

        // Pin đọc đồng bộ từ broadcast sticky để khung đầu tiên đã có dòng pin, bố cục không bị nhảy; giao diện đồng hồ cũng có ngay từ khung đầu.
        private fun initialState(
            dataStoreManager: DataStoreManager,
            batteryStateManager: BatteryStateManager,
        ): AodState = AodState(
            nowMillis = System.currentTimeMillis(),
            appearance = dataStoreManager.currentAodAppearance(),
            interaction = dataStoreManager.currentAodInteraction(),
            extras = dataStoreManager.currentAodExtras(),
            wallpaper = WallpaperValue.fromCode(dataStoreManager.aodWallpaper.value),
            battery = batteryStateManager.readLevelPercent()?.let { percent ->
                BatteryUiModel(
                    percent = percent,
                    isCharging = batteryStateManager.readIsCharging() == true
                )
            },
            shiftXDp = randomShiftX(),
            shiftYDp = randomShiftY(),
        )

        // Chống burn-in: mỗi phút nội dung sang một vị trí ngẫu nhiên.
        private fun randomShiftX(): Int = Random.nextInt(-MAX_SHIFT_X_DP, MAX_SHIFT_X_DP + 1)

        private fun randomShiftY(): Int = Random.nextInt(-MAX_SHIFT_Y_DP, MAX_SHIFT_Y_DP + 1)
    }
}
