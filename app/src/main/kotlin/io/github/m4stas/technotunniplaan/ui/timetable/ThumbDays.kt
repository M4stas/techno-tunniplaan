package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.lerp
import io.github.m4stas.technotunniplaan.ui.SlidingIndicator
import kotlin.math.abs
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

/**
 * The floating day picker for one week, placed within thumb reach above the navigation island.
 * [today] gets an accent outline so it stays findable while another day is selected. [position] is the index into
 * [days] that the day pager shows, fractional mid-swipe, so the highlight follows the swipe. Without it the
 * highlight sits on [selected].
 */
@Composable fun ThumbDays(days: List<LocalDate>, selected: LocalDate?, today: LocalDate?, onSelect: (LocalDate) -> Unit, modifier: Modifier = Modifier,
    position: (() -> Float)? = null) {
    val at = position ?: { days.indexOf(selected).toFloat() }
    val p = LocalPalette.current
    val locale = LocalConfiguration.current.locales[0]
    val letters = stringArrayResource(R.array.weekday_letters)
    Box(modifier.testTag("thumb-days").clip(CircleShape).background(p.island).padding(6.dp)) {
        SlidingIndicator(at, days.size, 44.dp, 4.dp, p.accent, visible = position != null || selected in days)
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            days.forEachIndexed { index, day ->
                val active = day == selected
                // How much of the highlight covers this day, so the label colours change along with the swipe. The position
                // is clamped like the indicator's, so a swipe past the week's last or first day keeps that day's label
                // on the accent instead of fading it while the indicator stays put.
                val cover = (1f - abs(at().coerceIn(0f, (days.size - 1).coerceAtLeast(0).toFloat()) - index)).coerceIn(0f, 1f)
                val description = "${dayTitle(day, locale)} ${day.dayOfMonth}"
                val letter = lerp(p.onIsland, p.onAccent, cover)
                val number = lerp(p.onIslandStrong, p.onAccent, cover)
                // The outline matches the sliding indicator's colour, so it disappears under it when today is selected.
                Column(Modifier.testTag("thumb-day-$day").size(44.dp).clip(CircleShape)
                    .then(if (day == today) Modifier.border(2.dp, p.accent, CircleShape) else Modifier)
                    .selectable(active, role = Role.Tab, onClick = { onSelect(day) })
                    .semantics { contentDescription = description },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(letters[day.dayOfWeek.value - 1], Modifier.clearAndSetSemantics { }, color = letter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(day.dayOfMonth.toString(), Modifier.clearAndSetSemantics { }, color = number, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
