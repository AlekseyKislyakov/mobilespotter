package com.example.mobile_spotter.data.navigator

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.example.mobile_spotter.R
import com.example.mobile_spotter.presentation.devicedetails.DeviceDetailsFragment
import com.example.mobile_spotter.presentation.devicelist.DeviceListFragment
import com.example.mobile_spotter.presentation.userlist.UserListFragment
import javax.inject.Inject

class AppNavigatorImpl @Inject constructor(private val activity: FragmentActivity) : AppNavigator {

    override fun navigateTo(screen: Screens, bundle: Bundle?) {
        val existingFragment = activity.supportFragmentManager.findFragmentByTag(
            detectScreen(
                screen,
                bundle
            )::class.java.canonicalName
        )
        if (existingFragment != null) {
            val trans: FragmentTransaction = activity.supportFragmentManager.beginTransaction()
            trans.remove(existingFragment)
            trans.commit()
            activity.supportFragmentManager.popBackStack()
        }

        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, detectScreen(screen, bundle))
            .addToBackStack(detectScreen(screen, bundle)::class.java.canonicalName)
            .commit()
    }

    override fun navigateToRoot(screen: Screens, bundle: Bundle?) {
        activity.supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, detectScreen(screen, bundle))
            .commit()
    }

    private fun detectScreen(screen: Screens, bundle: Bundle?): Fragment {
        return when (screen) {
            Screens.USERS -> UserListFragment().apply {
                bundle?.let {
                    arguments = it
                }
            }
            Screens.DEVICES -> DeviceListFragment().apply {
                bundle?.let {
                    arguments = it
                }
            }
            Screens.DEVICE_DETAILS -> DeviceDetailsFragment().apply {
                bundle?.let {
                    arguments = it
                }
            }
        }
    }
}
