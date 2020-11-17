package com.example.mobile_spotter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.mobile_spotter.presentation.base.BaseFragment
import com.example.mobile_spotter.utils.SessionHandler
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : FragmentActivity(R.layout.activity_main) {

    var snackbar: Snackbar? = null

    @Inject
    lateinit var sessionHandler: SessionHandler

    companion object {
        fun createStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        sessionHandler.onSessionRefreshed()
        sessionHandler.refreshListener = {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            (navHostFragment?.childFragmentManager?.fragments?.get(0) as? BaseFragment)?.logoutTimerEvent()
            sessionHandler.onSessionRefreshed()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        sessionHandler.onSessionRefreshed()
    }

    fun showShackbar(text: String, isLong: Boolean) {
        snackbar = Snackbar.make(
            nav_host_fragment,
            text,
            if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        )
        snackbar?.show()
    }

    fun showActionShackbar(text: String, block: (Unit) -> Unit) {
        snackbar = Snackbar.make(
            nav_host_fragment,
            text,
            Snackbar.LENGTH_LONG
        ).setAction(getString(R.string.common_navigate)) {
            block.invoke(Unit)
        }
        snackbar?.show()
    }

    fun hideSnackbar() {
        snackbar?.dismiss()
    }
}