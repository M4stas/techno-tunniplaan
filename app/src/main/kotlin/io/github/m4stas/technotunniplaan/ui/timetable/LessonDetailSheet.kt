package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LessonDetailSheet(lesson: Lesson, onDismiss: () -> Unit, onOpenEntity: (EntityRef) -> Unit) {
    val p = LocalPalette.current
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = p.card, contentColor = p.text) {
        Column(Modifier.testTag("lesson-details").verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(lesson.subject, style = MaterialTheme.typography.headlineSmall, color = p.text)
            Text(lessonText(lesson, EntityRef(EntityKind.CLASS, "")).time, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = p.accent)
            listOf(Triple(EntityKind.TEACHER, R.string.tab_teachers, lesson.teachers), Triple(EntityKind.CLASS, R.string.tab_classes, lesson.classes), Triple(EntityKind.ROOM, R.string.tab_rooms, lesson.rooms)).forEach { (kind, label, entities) ->
                if (entities.isNotEmpty()) Text(stringResource(label), color = p.textSecondary)
                entities.forEach { entity -> TextButton(onClick = { onDismiss(); onOpenEntity(EntityRef(kind, entity.key)) }, modifier = Modifier.fillMaxWidth()) { Text(entity.name, color = p.text) } }
            }
        }
    }
}
