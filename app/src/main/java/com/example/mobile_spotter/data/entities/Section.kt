package com.example.mobile_spotter.data.entities

data class Section(
    val id: String,
    val name: String,
    var isSelected: Boolean
)

const val ALL = "all"