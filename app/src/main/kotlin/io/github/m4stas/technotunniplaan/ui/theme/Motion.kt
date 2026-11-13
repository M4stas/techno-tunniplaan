package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
