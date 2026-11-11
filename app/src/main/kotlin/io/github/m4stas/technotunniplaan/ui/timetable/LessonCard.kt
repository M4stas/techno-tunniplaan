package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

@Composable fun LessonCard(lesson: Lesson, entity: EntityRef, highlighted: Boolean, modifier: Modifier = Modifier, onLessonClick: (Lesson) -> Unit = {}) {
    val p = LocalPalette.current
    val text = lessonText(lesson, entity)
    Surface(modifier = modifier.testTag("lesson-card"), shape = RoundedCornerShape(24.dp), color = if (highlighted) p.highlight else p.card) {
        Column(Modifier.then(if (highlighted) Modifier.testTag("lesson-highlighted") else Modifier).clickable { onLessonClick(lesson) }.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text.time, color = if (highlighted) p.accent else p.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text.title, color = p.text, style = MaterialTheme.typography.titleMedium)
            text.subGroup?.let { Text(it, color = p.textSecondary) }
            if (text.place.isNotEmpty()) Text(text.place, color = p.textSecondary)
            if (text.who.isNotEmpty()) Text(text.who, color = p.textSecondary)
        }
    }
}
