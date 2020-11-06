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

    private var currentNavController: LiveData<NavController>? = null

    companion object {
        private const val NAVIGATION_DEFAULT_ELEVATION = 16f
        private const val BADGE_CHARACTER_COUNT_MAX = 3

        fun createStartIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
//            setupBottomNavigationBar()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        return super.onKeyDown(keyCode, event)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
//        setupBottomNavigationBar()
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
        ).setAction("Перейти") {
            block.invoke(Unit)
        }.show()
    }

//    private fun setupBottomNavigationBar() {
//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
//
//        val navGraphIds = listOf(R.navigation.nav_graph, R.navigation.nav_graph_details)
//
//        // Setup the bottom navigation view with a list of navigation graphs
//        val controller = bottomNavigationView.setupWithNavController(
//            navGraphIds = navGraphIds,
//            fragmentManager = supportFragmentManager,
//            containerId = R.id.nav_host_fragment,
//            intent = intent
//        )
//
//        // Whenever the selected controller changes, setup the action bar.
//        controller.observe(this, Observer { navController ->
//            //setupBottomNavigationBar()
//        })
//        currentNavController = controller
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        return currentNavController?.value?.navigateUp() ?: false
//    }

//    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
//
//    }
}