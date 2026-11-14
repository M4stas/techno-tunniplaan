package io.github.m4stas.technotunniplaan.ui.theme

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Test

class TypographyTest {
    @Test fun everyTextStyleUsesInterTight() {
        val t = AppTypography
        val styles = listOf(t.displayLarge, t.displayMedium, t.displaySmall, t.headlineLarge, t.headlineMedium, t.headlineSmall,
            t.titleLarge, t.titleMedium, t.titleSmall, t.bodyLarge, t.bodyMedium, t.bodySmall, t.labelLarge, t.labelMedium, t.labelSmall)
        styles.forEach { assertEquals(InterTight, it.fontFamily) }
    }

    // Gradle runs unit tests with the module directory as the working directory.
    @Test fun bundledFontIsThePinnedFile() {
        val bytes = File("src/main/res/font/inter_tight.ttf").readBytes()
        val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        assertEquals("b81b73dcb64df3c230cabade7df6c5773bf863233f24c9ee51087519f1f88b6f", sha)
    }
}
