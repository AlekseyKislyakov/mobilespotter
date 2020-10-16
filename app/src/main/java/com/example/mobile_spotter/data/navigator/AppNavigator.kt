package com.example.mobile_spotter.data.navigator

import android.os.Bundle

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
    fun navigateTo(screen: Screens, bundle: Bundle? = null)
    fun navigateToRoot(screen: Screens, bundle: Bundle? = null)
}
