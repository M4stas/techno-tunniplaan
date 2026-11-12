package io.github.m4stas.technotunniplaan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import io.github.m4stas.technotunniplaan.AppContainer
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.store.Settings
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.onboarding.*
import io.github.m4stas.technotunniplaan.ui.browse.*
import io.github.m4stas.technotunniplaan.ui.theme.*
import io.github.m4stas.technotunniplaan.ui.timetable.*
import io.github.m4stas.technotunniplaan.schedule.EntityKind
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import java.io.IOException
import java.net.URLEncoder

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import io.github.m4stas.technotunniplaan.ui.settings.*

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val BROWSE = "browse"
    const val SETTINGS = "settings"
    fun entity(ref: EntityRef) = "entity/${ref.kind}/${URLEncoder.encode(ref.key, "UTF-8").replace("+", "%20")}"
}

/** [onOpened] tells the activity that [openEntity] is on screen, so the same entity can be asked for again. */
@Composable fun AppRoot(container: AppContainer, openEntity: EntityRef?, onOpened: () -> Unit) {
    val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(settings?.theme) {
        settings?.theme?.let { theme ->
            val mode = nightMode(theme)
            if (AppCompatDelegate.getDefaultNightMode() != mode) AppCompatDelegate.setDefaultNightMode(mode)
        }
    }
    val weeks by container.repository.weeks.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(container, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                container.repository.ensureLoaded()
                container.sync.syncIfStale()
            } catch (_: IOException) {
                // Keep the loaded data when storage is unavailable.
            }
        }
    }
    AppTheme(settings?.theme ?: ThemeMode.SYSTEM) {
        val view = LocalView.current
        val light = LocalPalette.current != DarkPalette
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = light
                    isAppearanceLightNavigationBars = light
                }
            }
        }
        Surface(Modifier.fillMaxSize(), color = LocalPalette.current.background) {
            val loaded = settings
            if (loaded != null && weeks != null) {
                AppShell(loaded.home, openEntity, onOpened, browse = { nav ->
                    val vm: BrowseViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T = BrowseViewModel(container.repository, container.settings) as T
                    })
                    val state by vm.state.collectAsStateWithLifecycle()
                    BrowseScreen(state, vm::setTab, vm::setQuery) { nav.navigate(Routes.entity(it)) }
                }, settings = { nav ->
                    val vm: SettingsViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(container.repository, container.settings, container.sync) as T
                    })
                    val state by vm.state.collectAsStateWithLifecycle()
                    val locales = AppCompatDelegate.getApplicationLocales()
                    SettingsScreen(state, { nav.navigate(Routes.ONBOARDING) }, vm::chooseGroup, vm::setTheme,
                        { tag -> AppCompatDelegate.setApplicationLocales(if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)) },
                        vm::syncNow, if (locales.isEmpty) null else locales[0]?.language)
                }, timetable = { entity, nav ->
                    val vm: TimetableViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T = TimetableViewModel(entity, container.repository, container.settings, container.sync, container.clock) as T
                    })
                    val state by vm.state.collectAsStateWithLifecycle()
                    val back: (() -> Unit)? = if (entity == null) null else { { nav.popBackStack() } }
                    state?.let { TimetableScreen(it, vm::selectDate, vm::refresh, vm::toggleFavourite,
                        { ref -> nav.navigate(Routes.entity(ref)) }, { nav.navigate(Routes.ONBOARDING) },
                        back) }
                }) { nav ->
                    val initialHome = remember { loaded.home }
                    val vm: OnboardingViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T = OnboardingViewModel(container.sync, container.repository, container.settings) as T
                    })
                    val state by vm.state.collectAsStateWithLifecycle()
                    val picked by vm.picked.collectAsStateWithLifecycle()
                    LaunchedEffect(picked) {
                        if (picked != null && initialHome != null && nav.previousBackStackEntry != null) {
                            nav.popBackStack()
                        }
                    }
                    OnboardingScreen(state, vm::chooseRole, vm::back, vm::setQuery, vm::pick, vm::retry)
                }
            }
        }
    }
}

/**
 * The navigation graph and the navigation island. The graph starts at home whenever a home is set, and an
 * entity from a widget opens on top of it, so the navigation island's `popUpTo(HOME)` always finds home.
 */
@Composable fun AppShell(
    home: EntityRef?, openEntity: EntityRef?, onOpened: () -> Unit,
    nav: NavHostController = rememberNavController(),
    timetable: @Composable (EntityRef?, NavHostController) -> Unit = { _, _ -> Text(stringResource(R.string.nav_timetable), Modifier.padding(24.dp), color = LocalPalette.current.text) },
    browse: @Composable (NavHostController) -> Unit = { Text(stringResource(R.string.nav_browse), Modifier.padding(24.dp), color = LocalPalette.current.text) },
    settings: @Composable (NavHostController) -> Unit = { Text(stringResource(R.string.nav_settings), Modifier.padding(24.dp), color = LocalPalette.current.text) },
    onboarding: @Composable (NavHostController) -> Unit,
) {
    val start = remember { if (home == null) Routes.ONBOARDING else Routes.HOME }
    val entry by nav.currentBackStackEntryAsState()
    val hasHome = home != null
    LaunchedEffect(openEntity, hasHome) {
        if (hasHome && nav.currentBackStackEntry?.destination?.route == Routes.ONBOARDING && nav.previousBackStackEntry == null) {
            nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true }; launchSingleTop = true }
        }
        if (openEntity != null && hasHome) {
            nav.navigate(if (openEntity == home) Routes.HOME else Routes.entity(openEntity)) { popUpTo(Routes.HOME); launchSingleTop = true }
            onOpened()
        }
    }
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        NavHost(nav, startDestination = start, modifier = Modifier.fillMaxSize()) {
            composable(Routes.ONBOARDING) { onboarding(nav) }
            composable(Routes.HOME) { timetable(null, nav) }
            composable(Routes.BROWSE) { browse(nav) }
            composable(Routes.SETTINGS) { settings(nav) }
            composable("entity/{kind}/{key}") { entry ->
                val kind = entry.arguments?.getString("kind")?.let(EntityKind::valueOf)
                val key = entry.arguments?.getString("key")
                if (kind != null && key != null) timetable(EntityRef(kind, key), nav)
            }
        }
        val route = entry?.destination?.route
        if (route != Routes.ONBOARDING) {
            NavIsland(route, { target -> nav.navigate(target) { popUpTo(Routes.HOME); launchSingleTop = true } },
                Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
        }
    }
}
