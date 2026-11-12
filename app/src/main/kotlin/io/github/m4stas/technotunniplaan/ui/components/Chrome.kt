package io.github.m4stas.technotunniplaan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

/** Bottom padding that lets the last item of a scrolling screen clear the floating islands. */
object Clearance {
    val Nav = 92.dp
    val NavAndDays = 156.dp
}

@Composable fun ScreenHeader(title: String, subtitle: String?, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    val p = LocalPalette.current
    Column(modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f).semantics { heading() }, color = p.text, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.9).sp)
            // The trailing slot takes no height, so a 48dp touch target leaves the header as tall as one without it.
            if (trailing != null) Box(Modifier.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity))
                layout(placeable.width, 0) { placeable.place(0, -placeable.height / 2) }
            }) { trailing() }
        }
        if (subtitle != null) Text(subtitle, color = p.textMuted, fontSize = 13.sp)
    }
}

/** A 40dp circle inside a 48dp touch target. */
@Composable fun CircleButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, active: Boolean = false) {
    val p = LocalPalette.current
    Box(modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(if (active) p.accent else p.surface), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(20.dp), tint = if (active) p.onAccent else p.text)
        }
    }
}

@Composable fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val p = LocalPalette.current
    Button(onClick, modifier.heightIn(min = 52.dp), enabled = enabled, shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = p.selected, contentColor = p.onSelected, disabledContainerColor = p.track, disabledContentColor = p.textFaint)) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Shown when no timetable can be loaded. The caller sizes it and makes it scroll where pull to refresh needs that. */
@Composable fun LoadFailed(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Column(modifier.padding(horizontal = 36.dp, vertical = 32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(p.surface).border(1.dp, p.cardOutline, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Warning, null, Modifier.size(28.dp), tint = p.textMuted)
        }
        Text(stringResource(R.string.offline_title), Modifier.padding(top = 30.dp), color = p.text, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(stringResource(R.string.offline_body), Modifier.padding(top = 6.dp), color = p.textMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
        PrimaryButton(stringResource(R.string.retry), onRetry, Modifier.padding(top = 22.dp).width(200.dp))
    }
}

@Composable fun IslandFade(modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Box(modifier.fillMaxWidth().height(170.dp).background(Brush.verticalGradient(0f to p.backgroundClear, 0.55f to p.background, 1f to p.background)).testTag("island-fade"))
}
