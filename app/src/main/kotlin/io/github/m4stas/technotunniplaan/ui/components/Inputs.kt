package io.github.m4stas.technotunniplaan.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.runtime.Composable
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

/** A pill track with one active pill. Full width by default. [compact] sizes it to its labels, for use inside a row. */
@Composable fun <T> Segmented(options: List<SegmentOption<T>>, selected: T, onSelect: (T) -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val p = LocalPalette.current
    Row(modifier.clip(CircleShape).background(p.track).padding(4.dp).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEach { option ->
            val active = option.value == selected
            val described = option.description
            val fill by animateColorAsState(if (active) p.selected else p.track, Motion.fast(), label = "segment-fill")
            val ink by animateColorAsState(if (active) p.onSelected else p.textSecondary, Motion.fast(), label = "segment-ink")
            Box((if (compact) Modifier else Modifier.weight(1f))
                .heightIn(min = if (compact) 40.dp else 44.dp).clip(CircleShape).background(fill)
                .selectable(active, role = Role.RadioButton, onClick = { onSelect(option.value) })
                .then(if (described != null) Modifier.semantics { contentDescription = described } else Modifier)
                .padding(horizontal = if (compact) 14.dp else 8.dp), contentAlignment = Alignment.Center) {
                Text(option.label, if (described != null) Modifier.clearAndSetSemantics { } else Modifier,
                    color = ink, fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1)
            }
        }
    }
}
