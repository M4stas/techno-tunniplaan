package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.schedule.*
import io.github.m4stas.technotunniplaan.ui.components.Chevron
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

private const val SEPARATOR = " · "
data class DetailRow(val ref: EntityRef, @StringRes val label: Int, val value: String)

fun detailRows(slot: SlotUi, viewer: EntityRef): List<DetailRow> {
    val lessons = slot.lines.map { it.lesson }
    val teachers = lessons.flatMap { lesson ->
        val rooms = lesson.rooms.joinToString(", ") { it.short }
        lesson.teachers.map {
            DetailRow(EntityRef(EntityKind.TEACHER, it.key),
                if (rooms.isEmpty()) R.string.kind_teacher else R.string.detail_teacher_room,
                it.name + if (rooms.isEmpty()) "" else SEPARATOR + rooms)
        }
    }
    val rooms = lessons.flatMap { it.rooms }.map { DetailRow(EntityRef(EntityKind.ROOM, it.key), R.string.kind_room, it.name) }
    val classes = lessons.flatMap { it.classes }.map { DetailRow(EntityRef(EntityKind.CLASS, it.key), R.string.kind_class, it.name) }
    return (teachers + rooms + classes).distinctBy { it.ref }.filter { it.ref != viewer }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LessonDetailSheet(slot: SlotUi, dayLabel: String, viewer: EntityRef, onDismiss: () -> Unit, onOpenEntity: (EntityRef) -> Unit) {
    val p = LocalPalette.current
    val locale = LocalConfiguration.current.locales[0]
    ModalBottomSheet(onDismissRequest = onDismiss, shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp), containerColor = p.sheet, contentColor = p.text,
        dragHandle = { Box(Modifier.padding(top = 12.dp, bottom = 18.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(p.outline)) }) {
        Column(Modifier.testTag("lesson-details").verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 22.dp, bottom = 34.dp)) {
            Text(dayLabel, color = p.textMuted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(slot.lines.map { it.subject }.distinct().joinToString(" / "), color = p.text, fontSize = 24.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.time_range, slot.start.format(HOUR_MINUTE), slot.end.format(HOUR_MINUTE)), color = p.accentText, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.03).em)
            Spacer(Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                detailRows(slot, viewer).forEach { row ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(p.track).clickable { onDismiss(); onOpenEntity(row.ref) }
                        .padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(row.label).uppercase(locale), color = p.textMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em)
                            Spacer(Modifier.height(3.dp))
                            Text(row.value, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Chevron()
                    }
                }
            }
        }
    }
}
