package com.example.mobile_spotter.data.entities

val OS_ALL = "all"
val OS_ANDROID = "android"
val OS_IOS = "ios"

data class DeviceFilter(
    var os: String = "all",
    val versionList: MutableList<String> = mutableListOf(),
    var resolutionHeight: Int = -1,
    var resolutionWidth: Int = -1,
    var onlyAvailable: Boolean = false,
    val nonPrivate: Boolean = false
)