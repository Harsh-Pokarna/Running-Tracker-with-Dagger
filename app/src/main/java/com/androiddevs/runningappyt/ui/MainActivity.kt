package com.androiddevs.runningappyt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.database.RunDAO
import com.androiddevs.runningappyt.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)
        init()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun init() {
        setViews()
        setListeners()
    }

    private fun setViews() {
        navController = (supportFragmentManager.findFragmentById(R.id.runNavHostFragment) as NavHostFragment).findNavController()
        bottomNavigationView.setupWithNavController(navController)
    }

    private fun setListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.runFragment -> bottomNavigationView.visibility = View.VISIBLE
                R.id.statisticsFragment -> bottomNavigationView.visibility = View.VISIBLE
                R.id.settingsFragment -> bottomNavigationView.visibility = View.VISIBLE
                R.id.setupFragment -> bottomNavigationView.visibility = View.GONE
                R.id.trackingFragment -> bottomNavigationView.visibility = View.GONE
            }
        }
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        setViews()
        if (intent?.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT) {
            navController.navigate(R.id.action_global_trackingFragment)
        }
    }
}

