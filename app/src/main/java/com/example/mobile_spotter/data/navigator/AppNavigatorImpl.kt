package com.example.mobile_spotter.data.navigator

import androidx.fragment.app.FragmentActivity
import com.example.mobile_spotter.R
import com.example.mobile_spotter.presentation.userlist.UserListFragment
import javax.inject.Inject

class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    override fun navigateTo(screen: Screens) {
        val fragment = when (screen) {
            Screens.USERS -> UserListFragment()
            Screens.DEVICES -> UserListFragment()
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            // .addToBackStack(fragment::class.java.canonicalName)
            .commit()
    }
}
