package com.example.mobile_spotter.ext

import java.util.*

fun String.containsNoCase(other: String): Boolean {
    return this.toLowerCase(Locale.ROOT).contains(other.toLowerCase(Locale.ROOT))
}