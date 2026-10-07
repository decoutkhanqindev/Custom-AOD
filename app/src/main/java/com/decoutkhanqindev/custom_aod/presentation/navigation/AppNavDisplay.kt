package com.decoutkhanqindev.custom_aod.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.decoutkhanqindev.custom_aod.data.network.connectivity.NetworkManager
import com.decoutkhanqindev.custom_aod.presentation.components.dialog.AodsNoInternetDialog
import com.decoutkhanqindev.custom_aod.presentation.screens.language.LanguageScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.main.MainScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.PermissionScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.splash.SplashScreen
import com.decoutkhanqindev.custom_aod.utils.navigateBack
import org.koin.compose.koinInject

@Composable
fun AppNavDisplay(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(SplashDestination)
    val networkManager: NetworkManager = koinInject()
    val isNetworkAvailable by networkManager.isAvailable.collectAsStateWithLifecycle()

    NavDisplay(
        entries = rememberDecoratedNavEntries(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<SplashDestination> { SplashScreen(backStack) }
                entry<LanguageDestination> { dest -> LanguageScreen(backStack = backStack, isFirstOpen = dest.isFirstOpen) }
                entry<PermissionDestination> { PermissionScreen(backStack) }
                entry<MainDestination> { MainScreen(backStack) }
                // TODO: Đăng ký entry<XxxDestination> { dest -> XxxScreen(...) } cho màn mới
            },
        ),
        modifier = modifier,
        onBack = { backStack.navigateBack() },
    )

    if (!isNetworkAvailable) AodsNoInternetDialog()
}
