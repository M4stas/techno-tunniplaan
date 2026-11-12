package io.github.m4stas.technotunniplaan.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.EntityKind
import io.github.m4stas.technotunniplaan.schedule.SCHOOL_ZONE
import io.github.m4stas.technotunniplaan.store.ThemeMode
import io.github.m4stas.technotunniplaan.ui.components.*
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable fun SettingsScreen(state: SettingsUiState, onChangeHome: () -> Unit,
    onTheme: (ThemeMode) -> Unit, onLanguage: (String?) -> Unit, onSyncNow: () -> Unit, language: String? = null) {
    val p = LocalPalette.current
    val auto = stringResource(R.string.option_auto)
    val system = stringResource(R.string.theme_system)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 14.dp)) {
        Text(stringResource(R.string.nav_settings), Modifier.semantics { heading() }, color = p.text,
            fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.84).sp)
        SectionLabel(stringResource(R.string.settings_home))
        StackRow(0, 1, onChangeHome, minHeight = 52.dp) {
            Text(state.homeTitle, Modifier.weight(1f), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            state.homeKind?.let { kind ->
                Text(stringResource(when (kind) {
                    EntityKind.CLASS -> R.string.kind_class
                    EntityKind.TEACHER -> R.string.kind_teacher
                    EntityKind.ROOM -> R.string.kind_room
                }), color = p.textMuted, fontSize = 13.sp)
            }
            Chevron()
        }
        SectionLabel(stringResource(R.string.settings_app))
        StackRow(0, 2, null, minHeight = 52.dp) {
            Text(stringResource(R.string.settings_theme), Modifier.weight(1f), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Segmented(listOf(
                SegmentOption(ThemeMode.SYSTEM, auto, system),
                SegmentOption(ThemeMode.DARK, stringResource(R.string.theme_dark), stringResource(R.string.theme_dark)),
                SegmentOption(ThemeMode.LIGHT, stringResource(R.string.theme_light), stringResource(R.string.theme_light)),
            ), state.theme, onTheme, dense = true)
        }
        Spacer(Modifier.height(4.dp))
        StackRow(1, 2, null, minHeight = 52.dp) {
            Text(stringResource(R.string.settings_language), Modifier.weight(1f), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Segmented(listOf(
                SegmentOption(null, auto, system),
                SegmentOption("et", stringResource(R.string.language_et_short), stringResource(R.string.language_et)),
                SegmentOption("en", stringResource(R.string.language_en_short), stringResource(R.string.language_en)),
            ), language, onLanguage, dense = true)
        }
        Row(Modifier.fillMaxWidth().padding(top = 14.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(syncTimeText(state.lastSyncMillis), Modifier.weight(1f), color = p.textMuted, fontSize = 12.sp)
            Box(Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clickable(enabled = !state.syncing, role = Role.Button, onClick = onSyncNow), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.sync_short), color = if (state.syncing) p.textFaint else p.accentText,
                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(Clearance.Nav))
    }
}

@Composable private fun SectionLabel(title: String) {
    Text(title, Modifier.padding(start = 4.dp, top = 18.dp, bottom = 6.dp),
        color = LocalPalette.current.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
}

@Composable private fun syncTimeText(millis: Long?): String {
    if (millis == null) return stringResource(R.string.never_synced)
    val instant = Instant.ofEpochMilli(millis)
    val locale = LocalConfiguration.current.locales[0]
    val time = DateTimeFormatter.ofPattern("HH:mm", locale).withZone(SCHOOL_ZONE).format(instant)
    return when (syncDay(instant, Instant.now())) {
        SyncDay.TODAY -> stringResource(R.string.last_synced_today, time)
        SyncDay.YESTERDAY -> stringResource(R.string.last_synced_yesterday, time)
        SyncDay.OTHER -> {
            val date = DateTimeFormatter.ofPattern(stringResource(R.string.short_date_pattern), locale).withZone(SCHOOL_ZONE).format(instant)
            stringResource(R.string.last_synced, "$date $time")
        }
    }
}
