package com.peecock.ymusic.ui.styling

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.peecock.ymusic.enums.ColorPaletteMode
import com.peecock.ymusic.utils.colorPaletteModeKey
import com.peecock.ymusic.utils.customThemeDark_Background0Key
import com.peecock.ymusic.utils.customThemeDark_Background1Key
import com.peecock.ymusic.utils.customThemeDark_Background2Key
import com.peecock.ymusic.utils.customThemeDark_Background3Key
import com.peecock.ymusic.utils.customThemeDark_Background4Key
import com.peecock.ymusic.utils.customThemeDark_TextKey
import com.peecock.ymusic.utils.customThemeDark_accentKey
import com.peecock.ymusic.utils.customThemeDark_iconButtonPlayerKey
import com.peecock.ymusic.utils.customThemeDark_textDisabledKey
import com.peecock.ymusic.utils.customThemeDark_textSecondaryKey
import com.peecock.ymusic.utils.customThemeLight_Background0Key
import com.peecock.ymusic.utils.customThemeLight_Background1Key
import com.peecock.ymusic.utils.customThemeLight_Background2Key
import com.peecock.ymusic.utils.customThemeLight_Background3Key
import com.peecock.ymusic.utils.customThemeLight_Background4Key
import com.peecock.ymusic.utils.customThemeLight_TextKey
import com.peecock.ymusic.utils.customThemeLight_accentKey
import com.peecock.ymusic.utils.customThemeLight_iconButtonPlayerKey
import com.peecock.ymusic.utils.customThemeLight_textDisabledKey
import com.peecock.ymusic.utils.customThemeLight_textSecondaryKey
import com.peecock.ymusic.utils.getEnum
import com.peecock.ymusic.utils.preferences


fun customColorPalette(colorPalette: ColorPalette, context: Context, isSystemInDarkTheme: Boolean): ColorPalette {
    val colorPaletteMode = context.preferences.getEnum(colorPaletteModeKey, ColorPaletteMode.Dark)

    val customThemeLight = colorPalette.copy(
        background0 = Color(context.preferences.getInt(customThemeLight_Background0Key, DefaultLightColorPalette.background0.hashCode())),
        background1 = Color(context.preferences.getInt(customThemeLight_Background1Key, DefaultLightColorPalette.background1.hashCode())),
        background2 = Color(context.preferences.getInt(customThemeLight_Background2Key, DefaultLightColorPalette.background2.hashCode())),
        background3 = Color(context.preferences.getInt(customThemeLight_Background3Key, DefaultLightColorPalette.background3.hashCode())),
        background4 = Color(context.preferences.getInt(customThemeLight_Background4Key, DefaultLightColorPalette.background4.hashCode())),
        text = Color(context.preferences.getInt(customThemeLight_TextKey, DefaultLightColorPalette.text.hashCode())),
        textSecondary = Color(context.preferences.getInt(customThemeLight_textSecondaryKey, DefaultLightColorPalette.textSecondary.hashCode())),
        textDisabled = Color(context.preferences.getInt(customThemeLight_textDisabledKey, DefaultLightColorPalette.textDisabled.hashCode())),
        iconButtonPlayer = Color(context.preferences.getInt(customThemeLight_iconButtonPlayerKey, DefaultLightColorPalette.iconButtonPlayer.hashCode())),
        accent = Color(context.preferences.getInt(customThemeLight_accentKey, DefaultLightColorPalette.accent.hashCode()))
    )

    val customThemeDark = colorPalette.copy(
        background0 = Color(context.preferences.getInt(customThemeDark_Background0Key, DefaultDarkColorPalette.background0.hashCode())),
        background1 = Color(context.preferences.getInt(customThemeDark_Background1Key, DefaultDarkColorPalette.background1.hashCode())),
        background2 = Color(context.preferences.getInt(customThemeDark_Background2Key, DefaultDarkColorPalette.background2.hashCode())),
        background3 = Color(context.preferences.getInt(customThemeDark_Background3Key, DefaultDarkColorPalette.background3.hashCode())),
        background4 = Color(context.preferences.getInt(customThemeDark_Background4Key, DefaultDarkColorPalette.background4.hashCode())),
        text = Color(context.preferences.getInt(customThemeDark_TextKey, DefaultDarkColorPalette.text.hashCode())),
        textSecondary = Color(context.preferences.getInt(customThemeDark_textSecondaryKey, DefaultDarkColorPalette.textSecondary.hashCode())),
        textDisabled = Color(context.preferences.getInt(customThemeDark_textDisabledKey, DefaultDarkColorPalette.textDisabled.hashCode())),
        iconButtonPlayer = Color(context.preferences.getInt(customThemeDark_iconButtonPlayerKey, DefaultDarkColorPalette.iconButtonPlayer.hashCode())),
        accent = Color(context.preferences.getInt(customThemeDark_accentKey, DefaultDarkColorPalette.accent.hashCode()))
    )

    return when (colorPaletteMode) {
        ColorPaletteMode.Dark, ColorPaletteMode.PitchBlack -> customThemeDark
        ColorPaletteMode.Light -> customThemeLight
        ColorPaletteMode.System -> when (isSystemInDarkTheme) {
            true -> customThemeDark
            false -> customThemeLight
        }
    }
}
