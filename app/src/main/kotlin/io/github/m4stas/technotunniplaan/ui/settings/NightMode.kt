package io.github.m4stas.technotunniplaan.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import io.github.m4stas.technotunniplaan.store.ThemeMode

fun nightMode(theme: ThemeMode): Int = when (theme) {
    ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
}
