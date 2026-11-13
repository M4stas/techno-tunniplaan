package io.github.m4stas.technotunniplaan.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import io.github.m4stas.technotunniplaan.ui.theme.Motion
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

@Composable fun PillSearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val hint = stringResource(R.string.search)
    BasicTextField(value, onValueChange, modifier.fillMaxWidth().testTag("search-field"), singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = p.text, fontSize = 15.sp), cursorBrush = SolidColor(p.accentText),
        decorationBox = { field ->
            Row(Modifier.clip(CircleShape).background(p.surface).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Search, null, Modifier.size(18.dp), tint = p.textMuted)
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(hint, color = p.textMuted, fontSize = 15.sp)
                    field()
                }
            }
        })
}

/** One choice of a [Segmented] control. A [description] replaces a shortened [label] for accessibility services. */
data class SegmentOption<out T>(val value: T, val label: String, val description: String? = null)

/**
 * A pill track with one active pill. Full width by default. [compact] sizes it to its labels, for use inside a row.
 * The active pill slides between choices, and each label takes the active colour as the pill covers it, as the
 * navigation and day islands do.
 */
@Composable fun <T> Segmented(options: List<SegmentOption<T>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier, compact: Boolean = false, dense: Boolean = false) {
    val p = LocalPalette.current
    val activeFill = if (dense) p.segmentActive else p.selected
    val activeInk = if (dense) p.onSegmentActive else p.onSelected
    val selectedIndex = options.indexOfFirst { it.value == selected }
    // Each choice's left edge and width in pixels, measured inside the track's padding.
    val bounds = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }
    val left = remember { Animatable(0f) }
    val width = remember { Animatable(0f) }
    var placed by remember { mutableStateOf(false) }
    val target = bounds[selectedIndex]
    LaunchedEffect(selectedIndex, target) {
        if (target == null) return@LaunchedEffect
        if (!placed) {
            left.snapTo(target.first)
            width.snapTo(target.second)
            placed = true
        } else coroutineScope {
            launch { left.animateTo(target.first, Motion.medium()) }
            launch { width.animateTo(target.second, Motion.medium()) }
        }
    }
    Box(modifier.clip(CircleShape).background(p.track).padding(if (dense) 3.dp else 4.dp).selectableGroup()) {
        if (selectedIndex >= 0) Box(Modifier.matchParentSize()) {
            Box(Modifier.layout { measurable, constraints ->
                val w = if (placed) width.value.roundToInt().coerceAtLeast(0) else 0
                val placeable = measurable.measure(Constraints.fixed(w, constraints.maxHeight))
                layout(w, constraints.maxHeight) { placeable.place(left.value.roundToInt(), 0) }
            }.testTag("segment-indicator").clip(CircleShape).background(activeFill))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            options.forEachIndexed { index, option ->
                val active = index == selectedIndex
                val described = option.description
                val cover = coverage(bounds[index], placed, active, left.value, width.value)
                Box((if (compact || dense) Modifier else Modifier.weight(1f))
                    .heightIn(min = if (dense) 36.dp else if (compact) 40.dp else 44.dp)
                    .onPlaced { bounds[index] = it.positionInParent().x to it.size.width.toFloat() }
                    .clip(CircleShape)
                    .selectable(active, role = Role.RadioButton, onClick = { onSelect(option.value) })
                    .then(if (described != null) Modifier.semantics { contentDescription = described } else Modifier)
                    .padding(horizontal = if (dense) 10.dp else if (compact) 14.dp else 8.dp), contentAlignment = Alignment.Center) {
                    Text(option.label, if (described != null) Modifier.clearAndSetSemantics { } else Modifier,
                        color = lerp(p.textSecondary, activeInk, cover), fontSize = if (dense) 12.sp else 13.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}

/** How much of a choice at [item] (left edge, width) the sliding pill covers, from 0 to 1. */
private fun coverage(item: Pair<Float, Float>?, placed: Boolean, active: Boolean, left: Float, width: Float): Float {
    if (item == null || !placed) return if (active) 1f else 0f
    val (itemLeft, itemWidth) = item
    if (itemWidth <= 0f) return if (active) 1f else 0f
    val overlap = minOf(left + width, itemLeft + itemWidth) - maxOf(left, itemLeft)
    return (overlap / itemWidth).coerceIn(0f, 1f)
}
