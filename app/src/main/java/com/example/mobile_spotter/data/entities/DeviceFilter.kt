package com.example.mobile_spotter.data.entities

const val OS_ALL = "all"
const val OS_ANDROID = "android"
const val OS_IOS = "ios"

const val ORDERING_AS_IS = "as_is"
const val ORDERING_BY_VERSION = "by_version"

const val ORDERING_INCREASING = "increasing"
const val ORDERING_DECREASING = "decreasing"

data class DeviceFilter(
    var os: String = OS_ALL,
    var orderingMain: String = ORDERING_AS_IS,
    var orderingAlphabetical: String = ORDERING_INCREASING,
    val versionSet: Set<String> = setOf(),
    var selectedVersionSet: MutableSet<String> = mutableSetOf(),
    val resolutionSet: Set<String> = setOf(),
    var selectedResolutionSet: MutableSet<String> = mutableSetOf(),
    var onlyAvailable: Boolean = false,
    var nonPrivate: Boolean = false,
    var onlyMine: Boolean = false
)