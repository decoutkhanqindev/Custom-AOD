package com.decoutkhanqindev.custom_aod.presentation.screens.splash

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.decoutkhanqindev.custom_aod.ads.AdsManager
import com.decoutkhanqindev.custom_aod.ads.ad_unit.AdUnitState
import com.decoutkhanqindev.custom_aod.data.local.datastore.DataStoreManager
import com.decoutkhanqindev.custom_aod.data.network.connectivity.NetworkManager
import com.decoutkhanqindev.custom_aod.presentation.navigation.LanguageDestination
import com.decoutkhanqindev.custom_aod.presentation.navigation.MainDestination
import com.decoutkhanqindev.custom_aod.utils.navigateTo
import org.koin.compose.koinInject

@Composable
fun SplashScreen(backStack: NavBackStack<NavKey>) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val dataStoreManager: DataStoreManager = koinInject()
    val networkManager: NetworkManager = koinInject()
    val adsManager: AdsManager = koinInject()
    val isNetworkAvailable by networkManager.isAvailable.collectAsStateWithLifecycle()
    val isConsentGathered by adsManager.isConsentGathered.collectAsStateWithLifecycle()
    val isMobileAdsInitialized by adsManager.isMobileAdsInitialized.collectAsStateWithLifecycle()
    val interSplash = adsManager.interSplash
    val interSplashState by interSplash.state.collectAsStateWithLifecycle()
    val handleNext = {
        if (dataStoreManager.isFirstOpen.value == true) {
            backStack.navigateTo(LanguageDestination(isFirstOpen = true), preserveState = false)
        } else {
            backStack.navigateTo(MainDestination, preserveState = false)
        }
    }

    SideEffect(isConsentGathered, isMobileAdsInitialized, isNetworkAvailable) {
        when {
            !isConsentGathered || !isNetworkAvailable -> Unit
            !adsManager.canRequestAds -> handleNext()
            isMobileAdsInitialized -> interSplash.load(context)
        }
    }

    LifecycleResumeEffect(interSplashState, isNetworkAvailable) {
        if (isNetworkAvailable) {
            when (interSplashState) {
                AdUnitState.LOADED -> activity?.let {
                    interSplash.show(
                        activity = it,
                        onAdShowed = handleNext,
                        onAdFailedToShow = handleNext,
                    )
                } ?: handleNext()

                AdUnitState.FAILED -> handleNext()

                else -> Unit
            }
        }

        onPauseOrDispose { }
    }

    BackHandler { }

    SplashContent()
}
