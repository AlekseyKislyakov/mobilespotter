package com.example.mobile_spotter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.mobile_spotter.data.navigator.AppNavigator
import com.example.mobile_spotter.data.navigator.Screens
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity(R.layout.activity_main) {

    @Inject lateinit var navigator: AppNavigator

    companion object {
        private const val NAVIGATION_DEFAULT_ELEVATION = 16f
        private const val BADGE_CHARACTER_COUNT_MAX = 3

        fun createStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator.navigateTo(Screens.USERS)
    }
}