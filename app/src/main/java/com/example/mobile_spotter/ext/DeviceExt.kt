package com.example.mobile_spotter.ext

import android.content.Context
import com.example.mobile_spotter.R
import com.example.mobile_spotter.data.entities.Device

fun Device.detailedName(context: Context): String {
    return if(nickname.isNotEmpty()) {
        context.getString(R.string.common_whitespace_brackets_separator, name, nickname)
    } else {
        name
    }
}

fun Device.detailedResolution(context: Context): String {
    return context.getString(R.string.common_whitespace_separator, osType, resolution)
}

fun Device.detailedVersion(context: Context): String {
    return context.getString(R.string.common_whitespace_separator, osType, osVersion)
}

fun Device.fullVersion(context: Context): String {
    return context.getString(R.string.common_whitespace_double_separator, osType, osVersion, resolution)
}

