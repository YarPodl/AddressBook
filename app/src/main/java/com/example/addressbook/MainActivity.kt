package com.example.addressbook

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.addressbook.data.ContactEntry
import com.example.addressbook.databinding.ActivityMainBinding
import com.example.databaseadapterapp.DatabaseAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    lateinit var databaseAdapter: DatabaseAdapter
    var position : Int = 0
    var currentContact : ContactEntry? = null
    var menuItems : List<String> = emptyList()
        set(value) {
            field = value
            invalidateOptionsMenu()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseAdapter = DatabaseAdapter(this)
        databaseAdapter.open();

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        //invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuItems.forEach { it: String -> menu.add(it) }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.title?.toString()) {
            resources.getString(R.string.deleteContact) -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                if (currentContact?.id != null)
                    databaseAdapter.delete(currentContact?.id!!)
                navController.navigate(R.id.action_SecondFragment_to_FirstFragment)
                return true
            }
            resources.getString(R.string.newContact) -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                currentContact = null
                navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}