package edu.utap.bookbuddy

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import edu.utap.bookbuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authUser: AuthUser
    private lateinit var navController: NavController
    private lateinit var appBarConfig: AppBarConfiguration
    private var showMenu = true

    override fun onCreate(savedInstanceState: Bundle?) {
        //Don't user default action bar
        setTheme(R.style.Theme_BookBuddy_NoActionBar)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController


        // ✅ Make both loginFragment and libraryFragment top-level (no back arrow)
        appBarConfig = AppBarConfiguration(setOf(R.id.libraryFragment, R.id.loginFragment))
        setupActionBarWithNavController(navController, appBarConfig)


        val spinner = binding.toolbar.findViewById<Spinner>(R.id.toolbarSpinner)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Library", "Store")
        )
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> if (navController.currentDestination?.id != R.id.libraryFragment) {
                        navController.navigate(R.id.libraryFragment)
                    }
                    1 -> if (navController.currentDestination?.id != R.id.storeFragment) {
                        navController.navigate(R.id.storeFragment)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 🔄 Update toolbar dynamically whenever dest changes
        navController.addOnDestinationChangedListener { _, destination, _ ->

            // Set these fragments to act as "home" fragments, i.e. no back button
            supportActionBar?.setDisplayHomeAsUpEnabled(
                destination.id != R.id.libraryFragment &&
                        destination.id != R.id.loginFragment &&
                        destination.id != R.id.storeFragment
            )

            // 🔁 Update spinner visibility
            val spinner = binding.toolbar.findViewById<Spinner>(R.id.toolbarSpinner)
            spinner.visibility = if (destination.id == R.id.loginFragment) View.GONE else View.VISIBLE

            // ✅ Sync spinner selection with current fragment
            when (destination.id) {
                R.id.libraryFragment -> if (spinner.selectedItemPosition != 0) {
                    spinner.setSelection(0, false)
                }
                R.id.storeFragment -> if (spinner.selectedItemPosition != 1) {
                    spinner.setSelection(1, false)
                }
            }

            // 🔁 Refresh toolbar menu visibility (e.g., logout button)
            showMenu = destination.id != R.id.loginFragment
            invalidateOptionsMenu()
        }


        authUser = AuthUser(activityResultRegistry)
        lifecycle.addObserver(authUser)

        //Observe user auth state
        authUser.observeUser().observe(this) { user ->
            val dest = navController.currentDestination?.id
            if (user.isInvalid() && dest != R.id.loginFragment) {
                navController.navigate(R.id.loginFragment)
            } else if (!user.isInvalid() && dest == R.id.loginFragment) {
                navController.navigate(R.id.libraryFragment)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.library_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                navController.navigate(R.id.settingsFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.settings)?.isVisible = showMenu
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}