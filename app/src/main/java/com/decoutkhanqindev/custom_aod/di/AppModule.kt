package com.decoutkhanqindev.custom_aod.di

import com.decoutkhanqindev.custom_aod.ads.AdsManager
import com.decoutkhanqindev.custom_aod.data.device.audio.AudioStateManager
import com.decoutkhanqindev.custom_aod.data.device.battery.BatteryStateManager
import com.decoutkhanqindev.custom_aod.data.device.flashlight.FlashlightManager
import com.decoutkhanqindev.custom_aod.data.device.light.AmbientLightManager
import com.decoutkhanqindev.custom_aod.data.device.media.MediaStateManager
import com.decoutkhanqindev.custom_aod.data.device.notification.NotificationStateManager
import com.decoutkhanqindev.custom_aod.data.device.permission.PermissionManager
import com.decoutkhanqindev.custom_aod.data.device.pickup.PickupGestureManager
import com.decoutkhanqindev.custom_aod.data.device.proximity.ProximityManager
import com.decoutkhanqindev.custom_aod.data.device.screen.ScreenStateManager
import com.decoutkhanqindev.custom_aod.data.local.background.BackgroundImageManager
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.local.locale.LanguageManager
import com.decoutkhanqindev.custom_aod.data.network.connectivity.NetworkManager
import com.decoutkhanqindev.custom_aod.presentation.aod.AodSession
import com.decoutkhanqindev.custom_aod.presentation.screens.aod.AodViewModel
import com.decoutkhanqindev.custom_aod.presentation.screens.language.LanguageViewModel
import com.decoutkhanqindev.custom_aod.presentation.screens.main.MainViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

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
    single { BackgroundImageManager(androidApplication()) }
    single { FlashlightManager(androidApplication()) }
    single { AmbientLightManager(androidApplication()) }
    single { PickupGestureManager(androidApplication()) }
}

val adsModule = module {
    single { AdsManager() }
}

val aodModule = module {
    single { AodSession(get()) }
}

val repositoryModule = module {
    // TODO: DataSource + Repository của project — single<XxxRepository> { XxxRepositoryImpl(get()) }
}

val useCaseModule = module {
    // TODO: UseCase của project — factory { GetXxxUseCase(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { (isFirstOpen: Boolean) -> LanguageViewModel(isFirstOpen, get(), get()) }
    viewModel { (isPreview: Boolean) -> AodViewModel(isPreview, get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

val appModules = listOf(
    managerModule,
    adsModule,
    aodModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)
