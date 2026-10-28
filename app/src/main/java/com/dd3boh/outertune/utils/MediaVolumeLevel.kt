package com.dd3boh.outertune.utils

import kotlin.math.roundToInt

fun mediaVolumePercent(index: Int, maximum: Int): Int =
    if (maximum <= 0) 0 else (index.coerceIn(0, maximum) * 100f / maximum).roundToInt()

fun mediaVolumeIndex(percent: Float, maximum: Int): Int =
    if (maximum <= 0) 0 else (percent.coerceIn(0f, 100f) * maximum / 100f).roundToInt()
