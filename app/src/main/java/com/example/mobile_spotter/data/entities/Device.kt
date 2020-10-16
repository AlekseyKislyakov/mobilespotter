package com.example.mobile_spotter.data.entities

import com.google.gson.annotations.SerializedName

data class Device(
    val comment: String,
    val id: Int,
    val name: String,
    val nickname: String,
    @SerializedName("os_type") val osType: String,
    @SerializedName("os_version") val osVersion: String,
    @SerializedName("owner_id") val ownerId: Int,
    val private: Boolean,
    val resolution: String,
    val shell: String,
    @SerializedName("token_uid") val tokenUid: String,
    val type: String,
    val uid: String
)