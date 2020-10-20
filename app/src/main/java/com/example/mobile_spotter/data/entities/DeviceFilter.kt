package com.example.mobile_spotter.data.entities

val OS_ALL = "all"
val OS_ANDROID = "android"
val OS_IOS = "ios"

data class DeviceFilter(
    var os: String = "all",
    val versionSet: MutableSet<String> = mutableSetOf(),
    val selectedVersionSet: MutableSet<String> = mutableSetOf(),
    val resolutionSet: MutableSet<String> = mutableSetOf(),
    val selectedResolutionSet: MutableSet<String> = mutableSetOf(),
    var onlyAvailable: Boolean = false,
    val nonPrivate: Boolean = false
)