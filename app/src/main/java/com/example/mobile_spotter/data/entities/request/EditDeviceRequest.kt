package com.example.mobile_spotter.data.entities.request

import com.google.gson.annotations.SerializedName

data class EditDeviceRequest(
    val id: Int,
    @SerializedName("owner_id") val ownerId: Int = 0
)