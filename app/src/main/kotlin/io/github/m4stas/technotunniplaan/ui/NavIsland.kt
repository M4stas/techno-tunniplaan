package io.github.m4stas.technotunniplaan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import kotlin.math.abs
import androidx.compose.ui.graphics.lerp
import io.github.m4stas.technotunniplaan.ui.theme.Motion
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.m4stas.technotunniplaan.R
import io.github.m4stas.technotunniplaan.ui.theme.LocalPalette

/** The floating bottom navigation. [current] is the current route, with Timetable active on home and entity timetables. */
@Composable fun NavIsland(current: String?, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val items = listOf(
        Triple(Routes.HOME, rememberVectorPainter(Icons.Filled.DateRange), stringResource(R.string.nav_timetable)),
        Triple(Routes.BROWSE, rememberVectorPainter(Icons.Filled.Search), stringResource(R.string.nav_browse)),
        Triple(Routes.SETTINGS, painterResource(R.drawable.ic_sliders), stringResource(R.string.nav_settings)),
    )
    fun isActive(route: String) = current == route || (route == Routes.HOME && current == "entity/{kind}/{key}")
    val activeIndex = items.indexOfFirst { isActive(it.first) }
    val indicator = remember { Animatable(activeIndex.coerceAtLeast(0).toFloat()) }
    LaunchedEffect(activeIndex) {
        if (activeIndex < 0) return@LaunchedEffect
        // The new screen builds in the frames right after the route changes, and on a phone one of those frames can
        // take 100 ms. A slide started then is over before the next frame is drawn, so the circle would jump.
        // Wait until frames arrive at a normal pace again, for a few frames at most, then slide.
        var last = withFrameNanos { it }
        for (attempt in 0 until 6) {
            val now = withFrameNanos { it }
            val steady = now - last < 25_000_000L
            last = now
            if (steady) break
        }
        indicator.animateTo(activeIndex.toFloat(), Motion.medium())
    }
    val position = indicator.value
    Box(modifier.testTag("nav-island").clip(CircleShape).background(p.island).padding(6.dp)) {
        SlidingIndicator({ indicator.value }, items.size, 48.dp, 4.dp, p.accent, visible = activeIndex >= 0)
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { index, (route, icon, label) ->
                val active = isActive(route)
                // The icon turns to the accent colour as the sliding circle covers it, as the day picker's labels do.
                val cover = if (activeIndex < 0) 0f else (1f - abs(position - index)).coerceIn(0f, 1f)
                val tint = lerp(p.onIsland, p.onAccent, cover)
                Box(Modifier.size(48.dp).clip(CircleShape)
                    .selectable(active, role = Role.Tab, onClick = { onNavigate(route) })
                    .semantics { contentDescription = label }, contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(22.dp), tint = tint)
                }
            }
        }
    }
}

/**
 * The accent circle behind the active item of an island row of [count] items of [size], spaced by [gap].
 * [position] is the item index, fractional while moving between items. It is read at layout time only, so a
 * position that changes every frame, such as a pager's scroll, moves the circle without recomposing the row.
 */
@Composable fun SlidingIndicator(position: () -> Float, count: Int, size: Dp, gap: Dp, color: Color, visible: Boolean = true) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, Motion.fast(), label = "indicator-alpha")
    Box(Modifier.offset { IntOffset(((size + gap).toPx() * position().coerceIn(0f, (count - 1).coerceAtLeast(0).toFloat())).roundToInt(), 0) }
        .size(size).graphicsLayer { this.alpha = alpha }.clip(CircleShape).background(color))
}
