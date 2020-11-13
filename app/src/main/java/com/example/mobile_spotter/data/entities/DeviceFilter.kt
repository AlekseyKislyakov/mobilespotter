package com.example.mobile_spotter.data.entities

const val OS_ALL = "all"
const val OS_ANDROID = "android"
const val OS_IOS = "ios"

data class DeviceFilter(
    var os: String = "all",
    val versionSet: Set<String> = setOf(),
    var selectedVersionSet: MutableSet<String> = mutableSetOf(),
    val resolutionSet: Set<String> = setOf(),
    var selectedResolutionSet: MutableSet<String> = mutableSetOf(),
    var onlyAvailable: Boolean = false,
    var nonPrivate: Boolean = false,
    var onlyMine: Boolean = false
)