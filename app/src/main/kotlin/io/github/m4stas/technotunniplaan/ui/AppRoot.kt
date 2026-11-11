package io.github.m4stas.technotunniplaan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import io.github.m4stas.technotunniplaan.AppContainer
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.EntityRef
import io.github.m4stas.technotunniplaan.store.Settings
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.onboarding.*
import io.github.m4stas.technotunniplaan.ui.theme.*
import java.net.URLEncoder

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val BROWSE = "browse"
    const val SETTINGS = "settings"
    fun entity(ref: EntityRef) = "entity/${ref.kind}/${URLEncoder.encode(ref.key, "UTF-8").replace("+", "%20")}"
}

@Composable fun AppRoot(container: AppContainer, openEntity: EntityRef?) {
    val settings by produceState<Settings?>(null, container) { container.settings.settings.collect { value = it } }
    val weeks by container.repository.weeks.collectAsState()
    LaunchedEffect(container) { container.repository.ensureLoaded(); container.sync.syncIfStale() }
    AppTheme(settings?.theme ?: ThemeMode.SYSTEM) {
        val p = LocalPalette.current
        Surface(Modifier.fillMaxSize(), color = p.background) {
            val loaded = settings
            if (loaded != null && weeks != null) {
                val nav = rememberNavController()
                val start = remember { if (loaded.home == null) Routes.ONBOARDING else if (openEntity != null && openEntity != loaded.home) Routes.entity(openEntity) else Routes.HOME }
                val entry by nav.currentBackStackEntryAsState()
                LaunchedEffect(openEntity) {
                    if (openEntity != null && loaded.home != null) nav.navigate(if (openEntity == loaded.home) Routes.HOME else Routes.entity(openEntity)) { launchSingleTop = true }
                }
                Column(Modifier.safeDrawingPadding()) {
                    NavHost(nav, startDestination = start, modifier = Modifier.weight(1f)) {
                        composable(Routes.ONBOARDING) {
                            val initialHome = remember { loaded.home }
                            val vm: OnboardingViewModel = viewModel(factory = object : ViewModelProvider.Factory {
                                @Suppress("UNCHECKED_CAST")
                                override fun <T : ViewModel> create(modelClass: Class<T>): T = OnboardingViewModel(container.sync, container.repository, container.settings) as T
                            })
                            val state by vm.state.collectAsState()
                            val picked by vm.picked.collectAsState()
                            LaunchedEffect(picked) {
                                if (picked != null) {
                                    if (initialHome != null && nav.previousBackStackEntry != null) nav.popBackStack()
                                    else nav.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                                }
                            }
                            OnboardingScreen(state, vm::chooseRole, vm::back, vm::setQuery, vm::pick, vm::retry)
                        }
                        composable(Routes.HOME) { Text(stringResource(R.string.nav_timetable), Modifier.padding(24.dp), color = p.text) }
                        composable(Routes.BROWSE) { Text(stringResource(R.string.nav_browse), Modifier.padding(24.dp), color = p.text) }
                        composable(Routes.SETTINGS) { Text(stringResource(R.string.nav_settings), Modifier.padding(24.dp), color = p.text) }
                        composable("entity/{kind}/{key}") { Text(stringResource(R.string.nav_timetable), Modifier.padding(24.dp), color = p.text) }
                    }
                    if (entry?.destination?.route != Routes.ONBOARDING) {
                        Surface(Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = p.surface) {
                            Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                listOf(Routes.HOME to R.string.nav_timetable, Routes.BROWSE to R.string.nav_browse, Routes.SETTINGS to R.string.nav_settings).forEach { (route, label) ->
                                    TextButton(onClick = { nav.navigate(route) { popUpTo(Routes.HOME); launchSingleTop = true } }) { Text(stringResource(label), color = if (entry?.destination?.route == route) p.accent else p.textSecondary) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
