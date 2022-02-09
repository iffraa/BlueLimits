package com.app.bluelimits.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.app.bluelimits.R
import com.app.bluelimits.databinding.ActivityDashboardBinding
import com.app.bluelimits.util.Constants
import com.google.android.material.navigation.NavigationView

import android.view.View
import androidx.navigation.*


class DashboardActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDashboardBinding
    private var drawerLayout: DrawerLayout? = null
    private lateinit var navController: NavController
    private lateinit var navView: NavigationView

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  setActionBarTitle()
        setSupportActionBar(binding.appBarMain2.toolbar)

        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                 R.id.nav_login,  R.id.userDashboardFragment, R.id.nav_home, R.id.nav_reservation_msg

            ), drawerLayout
        )
        //  appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

        //show back button
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val isLogin = intent.getBooleanExtra(Constants.IS_LOGIN, false)
        setNavGraphStart(navController, isLogin)

        // changeMenuIcon()
        hideLoginItems()
        changeMenuIcon()
    }

    /*  private fun changeMenuIcon() {
          getSupportActionBar()?.setHomeButtonEnabled(true);
          getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
          getSupportActionBar()?.setHomeAsUpIndicator(R.drawable.nav_icon);

      }*/

    @SuppressLint("ResourceType")
    private fun setNavGraphStart(navController: NavController, isLogin: Boolean) {
        if (isLogin) {
            navController.setGraph(R.navigation.nav_login)
        } else {
            navController.setGraph(R.navigation.nav_apply)
        }

    }

    //this shows the overflow icon
    /*  override fun onCreateOptionsMenu(menu: Menu): Boolean {
          // Inflate the menu; this adds items to the action bar if it is present.
          menuInflater.inflate(R.menu.main_activity2, menu)
          return true
      }*/


    override fun onSupportNavigateUp(): Boolean {
        // changeMenuIcon()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setActionBarTitle(title: String) {
        binding = ActivityDashboardBinding.inflate(layoutInflater)

        setSupportActionBar(binding.appBarMain2.toolbar)

        //    binding.appBarMain2.toolbar.findViewById<TextView>(R.id.toolbar_title).setText(title)
    }

    /* private fun clickOverflowIcon()
     {
         binding.appBarMain2.toolbar.setOnMenuItemClickListener({
             Navigation.findNavController(navView).navigate(R.id.nav_home);

             true
         })
     }*/

    fun makeUserDashboardStart() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.userDashboardFragment,
                R.id.nav_reservation_msg,
                R.id.nav_home
            ), drawerLayout
        )

        //show back button
        setupActionBarWithNavController(navController, appBarConfiguration)
        navController.graph.startDestination = R.id.userDashboardFragment
        navView.setupWithNavController(navController)

    }

    private fun hideLoginItems() {
        val menu: Menu = binding.navView.getMenu()
        val nav_logout = menu.findItem(R.id.nav_logout)
        nav_logout.setVisible(false)

        val nav_invite = menu.findItem(R.id.nav_invite)
        nav_invite.setVisible(false)

        val nav_profile = menu.findItem(R.id.nav_profile)
        nav_profile.setVisible(false)

        val nav_reservation = menu.findItem(R.id.nav_guest_space)
        nav_reservation.setVisible(false)

      //  val nav_my_contract = menu.findItem(R.id.nav_contract)
       // nav_my_contract.setVisible(false)

        val nav_req_services = menu.findItem(R.id.nav_services)
        nav_req_services.setVisible(false)

        val nav_pwd = menu.findItem(R.id.nav_update_pwd)
        nav_pwd.setVisible(false)


    }

    private fun showLoginItems() {
        val menu: Menu = binding.navView.getMenu()

        val nav_login = menu.findItem(R.id.nav_login)
        nav_login.setVisible(false)


        val nav_logout = menu.findItem(R.id.nav_logout)
        nav_logout.setVisible(true)

        val nav_pwd = menu.findItem(R.id.nav_update_pwd)
        nav_pwd.setVisible(true)

        val nav_invite = menu.findItem(R.id.nav_invite)
        nav_invite.setVisible(true)

        val nav_profile = menu.findItem(R.id.nav_profile)
        nav_profile.setVisible(true)

        val nav_reservation = menu.findItem(R.id.nav_guest_space)
        nav_reservation.setVisible(true)

     //   val nav_my_contract = menu.findItem(R.id.nav_contract)
     //   nav_my_contract.setVisible(true)

        val nav_req_services = menu.findItem(R.id.nav_services)
        nav_req_services.setVisible(false)


    }

    fun changeLoginDisplay(isLogin: Boolean) {
        val menu: Menu = binding.navView.getMenu()

        if (isLogin) {
            showLoginItems()
        } else {
            hideLoginItems()
        }
    }

    fun onCustomTBIconClick(action: NavDirections?) {
        if (this::binding.isInitialized) {
            binding.appBarMain2.tvListing.visibility = View.GONE

            val dashboardIcon = binding.appBarMain2.ivLogo
            dashboardIcon.setOnClickListener {
                action?.let { it1 -> navController.navigate(it1) }

            }

        }
    }

    fun navigateToVisitorsList(action: NavDirections) {
        val tvVisitors = binding.appBarMain2.tvListing;
        tvVisitors.visibility = View.VISIBLE

        tvVisitors.setOnClickListener {
            navController.navigate(action)
        }
    }

    fun changeMenuIcon() {
        navController.addOnDestinationChangedListener(NavController.OnDestinationChangedListener { navController: NavController, navDestination: NavDestination, bundle: Bundle? ->
            if (
                navDestination.getId() ==  R.id.nav_reservation_msg
                ||
                navDestination.getId() == R.id.nav_home
                || navDestination.getId() == R.id.userDashboardFragment
                || navDestination.getId() == R.id.nav_login
            ) {
                getSupportActionBar()?.setHomeButtonEnabled(true);
                getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
                getSupportActionBar()?.setHomeAsUpIndicator(R.drawable.nav_icon);
            }
        })

    }

    fun setPermissions(isPermitted: Boolean, isGuest: Boolean) {
        val menu: Menu = binding.navView.getMenu()

        if (isGuest) {
            val nav_guest = menu.findItem(R.id.nav_guest_space)
            nav_guest.setVisible(isPermitted)
        } else {
            val nav_visitor = menu.findItem(R.id.nav_invite)
            nav_visitor.setVisible(isPermitted)
        }

    }

}

