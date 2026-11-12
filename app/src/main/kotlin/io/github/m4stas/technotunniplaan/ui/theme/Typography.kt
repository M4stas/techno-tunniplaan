package io.github.m4stas.technotunniplaan.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.m4stas.technotunniplaan.R

/** One variable font file. Each entry picks its weight through the default variation settings. */
val InterTight = FontFamily(
    Font(R.font.inter_tight, FontWeight.Normal),
    Font(R.font.inter_tight, FontWeight.Medium),
    Font(R.font.inter_tight, FontWeight.SemiBold),
    Font(R.font.inter_tight, FontWeight.Bold),
)

private fun TextStyle.inter() = copy(fontFamily = InterTight)

val AppTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.inter(), displayMedium = displayMedium.inter(), displaySmall = displaySmall.inter(),
        headlineLarge = headlineLarge.inter(), headlineMedium = headlineMedium.inter(), headlineSmall = headlineSmall.inter(),
        titleLarge = titleLarge.inter(), titleMedium = titleMedium.inter(), titleSmall = titleSmall.inter(),
        bodyLarge = bodyLarge.inter(), bodyMedium = bodyMedium.inter(), bodySmall = bodySmall.inter(),
        labelLarge = labelLarge.inter(), labelMedium = labelMedium.inter(), labelSmall = labelSmall.inter(),
    )
}
