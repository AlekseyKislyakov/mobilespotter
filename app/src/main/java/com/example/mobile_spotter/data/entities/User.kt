package com.example.mobile_spotter.data.entities

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("Department") val department: Int,
    @SerializedName("FirstName") val firstName: String,
    @SerializedName("GitHub") val github: String,
    @SerializedName("Id") val id: Int,
    @SerializedName("LastName") val lastName: String,
    @SerializedName("Login") val login: String,
    @SerializedName("TG") val telegram: String,
    @SerializedName("Rfid") val rfid: String
) : UserEntity