package com.example.mobile_spotter.ext

import com.example.mobile_spotter.data.entities.Device
import java.util.*


fun Device.detailedResolution(): String {
    return "${osType} ${resolution}"
}

fun Device.detailedVersion(): String {
    return "${osType} ${osVersion}"
}

fun String.containsNoCase(other: String): Boolean {
    return this.toLowerCase(Locale.ROOT).contains(other.toLowerCase(Locale.ROOT))
}