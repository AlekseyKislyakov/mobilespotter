package com.example.mobile_spotter.data.entities

data class FullDeviceInfo(
    val device: Device,
    val owner: User? = null,
    var selected: Boolean = false
)