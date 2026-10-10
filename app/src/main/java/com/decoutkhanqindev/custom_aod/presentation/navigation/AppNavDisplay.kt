package com.decoutkhanqindev.custom_aod.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.decoutkhanqindev.custom_aod.data.network.connectivity.NetworkManager
import com.decoutkhanqindev.custom_aod.presentation.components.AppTagProvider
import com.decoutkhanqindev.custom_aod.presentation.components.dialog.AppNoInternetDialog
import com.decoutkhanqindev.custom_aod.presentation.screens.customize.CustomizeScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.language.LanguageScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.main.MainScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.permission.PermissionScreen
import com.decoutkhanqindev.custom_aod.presentation.screens.splash.SplashScreen
import com.decoutkhanqindev.custom_aod.utils.navigateBack
import com.decoutkhanqindev.custom_aod.utils.openWifiSettings
import org.koin.compose.koinInject

@Composable
fun AppNavDisplay(modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
                screen<SplashDestination> { SplashScreen(backStack) }
                screen<LanguageDestination> { dest ->
                    LanguageScreen(
                        backStack = backStack,
                        isFirstOpen = dest.isFirstOpen
                    )
                }
                screen<CustomizeDestination> { CustomizeScreen(backStack) }
                screen<PermissionDestination> { PermissionScreen(backStack) }
                screen<MainDestination> { MainScreen(backStack) }
                // TODO: Đăng ký screen<XxxDestination> { dest -> XxxScreen(...) } cho màn mới
            },
        ),
        modifier = modifier,
        onBack = { backStack.navigateBack() },
    )

    if (!isNetworkAvailable) AppNoInternetDialog {
        context.openWifiSettings { networkManager.isAvailable.value }
    }
}

// Mỗi màn được cấp LocalTag = tên class của destination (vd MainDestination), cùng ý với interface Tag của lớp thường.
private inline fun <reified K : NavKey> EntryProviderScope<NavKey>.screen(
    crossinline content: @Composable (K) -> Unit,
) {
    entry<K> { dest ->
        AppTagProvider(tag = K::class.java.simpleName) { content(dest) }
    }
}
