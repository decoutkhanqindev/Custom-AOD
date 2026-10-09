package com.decoutkhanqindev.custom_aod.di

import com.decoutkhanqindev.custom_aod.ads.AdsManager
import com.decoutkhanqindev.custom_aod.data.device.audio.AudioStateManager
import com.decoutkhanqindev.custom_aod.data.device.battery.BatteryStateManager
import com.decoutkhanqindev.custom_aod.data.device.flashlight.FlashlightManager
import com.decoutkhanqindev.custom_aod.data.device.light.AmbientLightManager
import com.decoutkhanqindev.custom_aod.data.device.location.DeviceLocationManager
import com.decoutkhanqindev.custom_aod.data.device.media.MediaStateManager
import com.decoutkhanqindev.custom_aod.data.device.notification.NotificationStateManager
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.device.pickup.PickupGestureManager
import com.decoutkhanqindev.custom_aod.data.device.proximity.ProximityManager
import com.decoutkhanqindev.custom_aod.data.device.screen.ScreenStateManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.image.AodImageManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.data.network.api.OpenMeteoApiService
import com.decoutkhanqindev.custom_aod.data.network.connectivity.NetworkManager
import com.decoutkhanqindev.custom_aod.data.repository.CalendarRepositoryImpl
import com.decoutkhanqindev.custom_aod.data.repository.WeatherRepositoryImpl
import com.decoutkhanqindev.custom_aod.domain.repository.CalendarRepository
import com.decoutkhanqindev.custom_aod.domain.repository.WeatherRepository
import com.decoutkhanqindev.custom_aod.domain.usecase.GetUpcomingEventsUseCase
import com.decoutkhanqindev.custom_aod.domain.usecase.ObserveWeatherUseCase
import com.decoutkhanqindev.custom_aod.domain.usecase.RefreshWeatherUseCase
import com.decoutkhanqindev.custom_aod.presentation.aod.AodSession
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.AodViewModel
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.CustomizeViewModel
import com.decoutkhanqindev.custom_aod.presentation.screens.language.LanguageViewModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.MainViewModel
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.PermissionViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val managerModule = module {
    single { DataStoreManager(androidApplication()) }
    single { LanguageManager(androidApplication()) }
    single { NetworkManager(androidApplication()) }
    single { PermissionManager(androidApplication()) }
    single { ScreenStateManager(androidApplication()) }
    single { BatteryStateManager(androidApplication()) }
    single { AudioStateManager(androidApplication()) }
    single { ProximityManager(androidApplication()) }
    single { NotificationStateManager(androidApplication()) }
    single { MediaStateManager(androidApplication(), get()) }
    single { AodImageManager(androidApplication()) }
    single { FlashlightManager(androidApplication()) }
    single { AmbientLightManager(androidApplication()) }
    single { PickupGestureManager(androidApplication()) }
    single { DeviceLocationManager(androidApplication()) }
}

val networkModule = module {
    // Open-Meteo trả nhiều trường hơn app dùng: bỏ qua trường lạ thay vì lỗi khi đọc.
    single { Json { ignoreUnknownKeys = true } }
    single {
        Retrofit.Builder()
            .baseUrl(OpenMeteoApiService.BASE_URL)
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(OpenMeteoApiService::class.java) }
}

val adsModule = module {
    single { AdsManager() }
}

val aodModule = module {
    single { AodSession(get()) }
}

val repositoryModule = module {
    single<WeatherRepository> { WeatherRepositoryImpl(get(), get(), get()) }
    single<CalendarRepository> { CalendarRepositoryImpl(androidApplication()) }
}

val useCaseModule = module {
    factory { ObserveWeatherUseCase(get()) }
    factory { RefreshWeatherUseCase(get()) }
    factory { GetUpcomingEventsUseCase(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (isFirstOpen: Boolean) -> LanguageViewModel(isFirstOpen, get(), get()) }
    viewModel { CustomizeViewModel(get(), get(), get(), get()) }
    viewModel { PermissionViewModel(get(), get()) }
    viewModel { (isPreview: Boolean) ->
        AodViewModel(isPreview, get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
}

val appModules = listOf(
    managerModule,
    networkModule,
    adsModule,
    aodModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
