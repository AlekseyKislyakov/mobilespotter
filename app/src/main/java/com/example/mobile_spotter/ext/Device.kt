package com.example.mobile_spotter.ext

import com.example.mobile_spotter.data.entities.Device

fun Device.detailedName(): String {
    return "$name ($nickname)"
}

fun Device.detailedResolution(): String {
    return "$osType $resolution"
}

fun Device.detailedVersion(): String {
    return "$osType $osVersion"
}

fun Device.fullVersion(): String {
    return "$osType $osVersion $resolution"
}

