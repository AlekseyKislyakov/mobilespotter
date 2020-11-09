package com.example.mobile_spotter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.Observer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

@AndroidEntryPoint
class MainActivity : FragmentActivity(R.layout.activity_main) {

    companion object {
        fun createStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    fun showShackbar(text: String, isLong: Boolean) {
        Snackbar.make(
            nav_host_fragment,
            text,
            if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT
        ).show()
    }

    fun showActionShackbar(text: String, block: (Unit) -> Unit) {
        Snackbar.make(
                nav_host_fragment,
                text,
                Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.common_navigate)) {
            block.invoke(Unit)
        }.show()
    }
}