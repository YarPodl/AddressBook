package com.example.addressbook

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.Image
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.DrawableUtils
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.addressbook.data.ContactEntry
import com.example.addressbook.databinding.ActivityMainBinding
import com.example.databaseadapterapp.DatabaseAdapter
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.util.stream.Stream
import android.graphics.Bitmap.CompressFormat
import android.location.Location
import android.location.LocationManager
import android.media.MediaPlayer
import java.io.ByteArrayOutputStream
import android.media.RingtoneManager

import android.media.Ringtone
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var playerDel : MediaPlayer
    private lateinit var playerNew : MediaPlayer
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    lateinit var databaseAdapter: DatabaseAdapter
    var position : Int = 0
    var currentContact : ContactEntry? = null
    var menuItems : List<String> = emptyList()
        set(value) {
            field = value
            invalidateOptionsMenu()
        }

    private var latestTmpUri: Uri? = null

    var lastLocation : Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseAdapter = DatabaseAdapter(this)
        databaseAdapter.open();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        playerDel = MediaPlayer.create(this, R.raw.del)
        playerNew = MediaPlayer.create(this, R.raw.soundnew)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
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

                playerDel!!.start()

                navController.navigate(R.id.action_SecondFragment_to_FirstFragment)
                return true
            }
            resources.getString(R.string.newContact) -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                currentContact = null
                playerNew!!.start()
                navController.navigate(R.id.action_FirstFragment_to_SecondFragment)
                return true
            }
            resources.getString(R.string.select_Image) -> {
                selectImageFromGallery()
                return true
            }
            resources.getString(R.string.currentLocation) -> {
                getLastLocation()
                if (lastLocation != null) {
                    val myDialogFragment = MessageDialog(lastLocation!!)
                    val manager = supportFragmentManager
                    myDialogFragment.show(manager, "myDialog")
                }
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

    private val takeImageResult = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            latestTmpUri?.let { uri ->
                setContactImage(uri)
            }
        }
    }

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { setContactImage(uri) }
    }

    fun takeImage() {
        lifecycleScope.launchWhenStarted {
            getTmpFileUri().let { uri ->
                latestTmpUri = uri
                takeImageResult.launch(uri)
            }
        }
    }

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", tmpFile)
    }

    private fun setContactImage(uri: Uri) {
        try {
            val contactImage = findViewById<ImageView>(R.id.contactImage)
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                contactImage.setImageBitmap(bitmap)
                currentContact?.image = getByteArrayFromBitmap(bitmap)
            }
        }
        catch (e: Exception) {

        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Granted. Start getting the location information
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        lastLocation = location
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()!!)
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            lastLocation = locationResult.lastLocation
        }
    }

    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray? {
        val bos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, bos)
        return bos.toByteArray()
    }

    private val PERMISSION_ID = 111222
}