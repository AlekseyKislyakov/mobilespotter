package com.example.mobile_spotter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.fragment.app.FragmentActivity
import com.example.mobile_spotter.ext.showSnackbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : FragmentActivity(R.layout.activity_main) {

    var snackbar: Snackbar? = null

    companion object {
        fun createStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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