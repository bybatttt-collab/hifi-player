package com.dd3boh.outertune.ui.theme

import androidx.compose.ui.text.font.FontFamily

/** The digital face selected from the earlier hardware-player reference. */
val HifiPlayerTextFont = HifiTitleFont

/** Digital lettering is an accent; long names and other scripts use the device's readable font. */
fun usesHifiDisplayFont(text: String): Boolean = text.length <= 28 && text.codePoints().allMatch {
    when (Character.UnicodeScript.of(it)) {
        Character.UnicodeScript.LATIN, Character.UnicodeScript.COMMON, Character.UnicodeScript.INHERITED -> true
        else -> false
    }
}

fun hifiPlayerFont(text: String): FontFamily = if (usesHifiDisplayFont(text)) HifiPlayerTextFont else FontFamily.Default
