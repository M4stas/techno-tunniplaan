package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.Lesson
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter

@Composable fun WeekView(grid: WeekGrid?, onLessonClick: (Lesson) -> Unit) {
    val p = LocalPalette.current
    val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
    if (grid == null || grid.periods.isEmpty()) {
        Text(stringResource(R.string.no_lessons), Modifier.testTag("week-grid").padding(16.dp), color = p.text)
        return
    }
    Row(Modifier.testTag("week-grid").verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState()).padding(16.dp)) {
        Column(Modifier.width(64.dp)) {
            Spacer(Modifier.height(40.dp))
            grid.periods.forEach { period -> Column(Modifier.height(88.dp)) {
                Text(period.number.toString(), color = p.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(period.start.format(DateTimeFormatter.ofPattern("HH:mm")), color = p.textSecondary, style = MaterialTheme.typography.labelMedium)
            } }
        }
        grid.days.forEach { day -> Column(Modifier.width(136.dp)) {
            Text(DayOfWeek.of(day + 1).getDisplayName(TextStyle.SHORT, locale), Modifier.height(40.dp), color = p.textSecondary)
            Box(Modifier.height((grid.periods.size * 88).dp).fillMaxWidth()) {
                grid.cells.filter { it.day == day }.forEach { cell ->
                    val start = grid.periods.indexOfFirst { it.number == cell.firstPeriod }
                    val span = grid.periods.count { it.number in cell.firstPeriod..cell.lastPeriod }
                    Row(Modifier.offset(y = (start * 88).dp).height((span * 88).dp).fillMaxWidth().padding(3.dp)) {
                        cell.lessons.forEach { lesson -> Surface(onClick = { onLessonClick(lesson) }, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(24.dp), color = p.card) {
                            Column(Modifier.padding(10.dp)) {
                                Text(lesson.subjectShort, color = p.text, fontWeight = FontWeight.Bold)
                                Text(lesson.rooms.joinToString(", ") { it.short }, color = p.textSecondary)
                            }
                        } }
                    }
                }
            }
        } }
    }
}
