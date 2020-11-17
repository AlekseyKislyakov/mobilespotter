package com.example.mobile_spotter.utils

interface SessionHandler {

    var refreshListener: () -> Unit

    fun onSessionRefreshed()
    fun clearListeners()
}