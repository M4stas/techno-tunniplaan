package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.unit.IntOffset

/** Shared animation specs. Durations stay short so the app feels smooth without slowing taps down. */
object Motion {
    const val FAST = 150
    const val MEDIUM = 220

    fun <T> fast() = tween<T>(FAST, easing = FastOutSlowInEasing)
    fun <T> medium() = tween<T>(MEDIUM, easing = FastOutSlowInEasing)
    fun <T> enter() = tween<T>(MEDIUM, easing = LinearOutSlowInEasing)

    val offset = spring<IntOffset>(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)
}

/**
 * Suspends until frames arrive at a normal pace again, for a few frames at most. Call it before a slide that follows
 * heavy work, such as a new screen or a theme change. On a phone one of those frames can take 100 ms, and a slide
 * started then is over before the next frame is drawn, so it would jump.
 */
suspend fun awaitSteadyFrames() {
    var last = withFrameNanos { it }
    for (attempt in 0 until 6) {
        val now = withFrameNanos { it }
        val steady = now - last < 25_000_000L
        last = now
        if (steady) break
    }
}
