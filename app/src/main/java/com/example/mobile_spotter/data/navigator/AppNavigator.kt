package com.example.mobile_spotter.data.navigator

/**
 * Available screens.
 */
enum class Screens {
    USERS,
    DEVICES
}

/**
 * Interfaces that defines an app navigator.
 */
interface AppNavigator {
    // Navigate to a given screen.
    fun navigateTo(screen: Screens)
}
