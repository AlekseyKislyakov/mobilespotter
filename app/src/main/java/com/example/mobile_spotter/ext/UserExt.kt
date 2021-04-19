package com.example.mobile_spotter.ext

import com.example.mobile_spotter.data.entities.User

fun User.fullName(): String {
    return "$lastName $firstName"
}