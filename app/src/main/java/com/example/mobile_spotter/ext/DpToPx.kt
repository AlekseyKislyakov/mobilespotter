package com.example.mobile_spotter.ext

import android.content.res.Resources
import kotlin.math.roundToInt

fun Int.dpToPx(): Int {
    val density = Resources.getSystem().displayMetrics.density
    return (this * density).roundToInt()
}

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
