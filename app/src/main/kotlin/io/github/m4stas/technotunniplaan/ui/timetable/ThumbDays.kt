package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.LocalDate

/** The floating day picker for one week, placed within thumb reach above the navigation island. */
@Composable fun ThumbDays(days: List<LocalDate>, selected: LocalDate?, onSelect: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val locale = LocalConfiguration.current.locales[0]
    val letters = stringArrayResource(R.array.weekday_letters)
    Row(modifier.testTag("thumb-days").clip(CircleShape).background(p.island).padding(6.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        days.forEach { day ->
            val active = day == selected
            val description = "${dayTitle(day, locale)} ${day.dayOfMonth}"
            val colour = if (active) p.onAccent else p.onIsland
            Column(Modifier.testTag("thumb-day-$day").size(44.dp).clip(CircleShape).background(if (active) p.accent else p.island)
                .selectable(active, role = Role.Tab, onClick = { onSelect(day) })
                .semantics { contentDescription = description },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(letters[day.dayOfWeek.value - 1], Modifier.clearAndSetSemantics { }, color = colour, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(day.dayOfMonth.toString(), Modifier.clearAndSetSemantics { }, color = if (active) p.onAccent else p.onIslandStrong, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
