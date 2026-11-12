package io.github.m4stas.technotunniplaan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

/** Rows of one list sit 4dp apart. Only the list's outer corners get the large radius. */
fun stackShape(index: Int, count: Int): RoundedCornerShape {
    val top = if (index == 0) 18.dp else 6.dp
    val bottom = if (index == count - 1) 18.dp else 6.dp
    return RoundedCornerShape(top, top, bottom, bottom)
}

@Composable fun StackRow(index: Int, count: Int, onClick: (() -> Unit)?, modifier: Modifier = Modifier, selected: Boolean = false, content: @Composable RowScope.() -> Unit) {
    val p = LocalPalette.current
    val shape = stackShape(index, count)
    Row(modifier.fillMaxWidth().clip(shape).background(if (selected) p.selectedRow else p.card).border(1.dp, p.cardOutline, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable fun Chevron() {
    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp), tint = LocalPalette.current.textFaint)
}
