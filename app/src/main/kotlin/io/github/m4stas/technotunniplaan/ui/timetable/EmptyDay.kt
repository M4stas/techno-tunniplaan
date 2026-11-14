package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

/** A day without lessons. [next] is the entity's next lesson after now, on whichever day it falls. */
@Composable fun EmptyDay(next: NextLessonUi?, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Column(modifier.testTag("empty-day").padding(horizontal = 16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        // Decoration. "No lessons" below says the same thing to accessibility services.
        Text(stringResource(R.string.free_day), Modifier.clearAndSetSemantics { }, color = p.ghost, fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.05).em, lineHeight = 64.sp)
        Text(stringResource(R.string.no_lessons), Modifier.padding(top = 12.dp), color = p.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        if (next != null) Text(nextLessonText(next), Modifier.padding(top = 6.dp), color = p.textMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}

@Composable private fun nextLessonText(next: NextLessonUi): String {
    val time = next.start.format(HOUR_MINUTE)
    return when {
        next.daysAhead == 0 -> stringResource(R.string.next_lesson_today, time)
        next.daysAhead <= 6 -> stringResource(R.string.next_lesson_weekday, stringArrayResource(R.array.weekday_on)[next.date.dayOfWeek.value - 1], time)
        else -> stringResource(R.string.next_lesson_date, dateLabel(next.date, stringResource(R.string.short_date_pattern), LocalConfiguration.current.locales[0]), time)
    }
}
