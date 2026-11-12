package io.github.m4stas.technotunniplaan.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

private const val SEPARATOR = " · "

/** One row of the day timeline: the slot's times on the left and its card on the right. */
@Composable fun TimelineRow(slot: SlotUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val current = slot.status == SlotStatus.CURRENT
    Row(modifier.fillMaxWidth().alpha(if (slot.status == SlotStatus.PAST) 0.4f else 1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.width(46.dp).padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(slot.start.format(HOUR_MINUTE), color = if (current) p.accentText else p.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(slot.end.format(HOUR_MINUTE), color = if (current) p.accentTextSecondary else p.textFaint, fontSize = 12.sp)
        }
        SlotCard(slot, onClick, Modifier.weight(1f))
    }
}

@Composable fun SlotCard(slot: SlotUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val current = slot.status == SlotStatus.CURRENT
    val shape = RoundedCornerShape(20.dp)
    val inProgress = stringResource(R.string.lesson_in_progress)
    val tint = p.accentTint
    val title = if (current) p.accentText else p.text
    val sub = if (current) p.textSecondary else p.textMuted
    // The elapsed part of the current lesson is tinted from the left edge.
    val progress = if (current) Modifier.testTag("slot-current")
        .semantics { progressBarRangeInfo = ProgressBarRangeInfo(slot.progress, 0f..1f); stateDescription = inProgress }
        .drawBehind { drawRect(tint, size = Size(size.width * slot.progress, size.height)) } else Modifier
    Box(modifier.testTag("slot-card").clip(shape).background(if (current) p.currentCard else p.card)
        .border(1.dp, if (current) p.currentOutline else p.cardOutline, shape).clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().then(progress).padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(if (slot.lines.size > 1) 0.dp else 2.dp)) {
            if (slot.lines.size == 1) {
                val line = slot.lines.first()
                Text(line.subject, color = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                val detail = listOf(line.who, line.place).filter { it.isNotEmpty() }.joinToString(SEPARATOR)
                if (detail.isNotEmpty()) Text(detail, color = sub, fontSize = 12.sp)
            } else {
                if (slot.sameSubject) Text(slot.lines.first().subject, color = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                slot.lines.forEachIndexed { index, line ->
                    if (index > 0) Box(Modifier.padding(vertical = 6.dp).fillMaxWidth().height(1.dp).background(p.divider))
                    else if (slot.sameSubject) Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = if (slot.sameSubject) Alignment.CenterVertically else Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            if (!slot.sameSubject) Text(line.subject, color = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            val detail = listOfNotNull(line.time, line.who.ifEmpty { null }).joinToString(SEPARATOR)
                            if (detail.isNotEmpty()) Text(detail, color = p.textMuted, fontSize = 12.sp)
                        }
                        if (line.place.isNotEmpty()) Text(line.place, color = p.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** The pause before a slot. It lines up with the cards, past the 46dp time column and the 12dp gap. */
@Composable fun BreakLabel(pause: BreakUi, modifier: Modifier = Modifier) {
    val label = when (pause.kind) {
        BreakKind.BREAK -> R.string.break_short
        BreakKind.LUNCH -> R.string.break_lunch
        BreakKind.FREE -> R.string.break_free
    }
    Text(stringResource(label, pause.minutes), modifier.testTag("break-label").padding(start = 58.dp), color = LocalPalette.current.textFaint, fontSize = 11.sp)
}