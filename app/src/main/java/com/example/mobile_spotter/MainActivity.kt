package com.example.mobile_spotter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import dagger.hilt.android.AndroidEntryPoint

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

//        bottomNavigationView.setupWithNavController(
//            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController.apply {
//                addOnDestinationChangedListener(this@MainActivity)
//                NavigationUI.setupWithNavController(bottomNavigationView, this)
//            }
//        )
//
//
//        bottomNavigationView.setOnNavigationItemSelectedListener {item ->
//            onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment))
//        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
//        setupBottomNavigationBar()
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