package com.example.addressbook

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
import java.io.ByteArrayOutputStream


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
            resources.getString(R.string.select_Image) -> {
                selectImageFromGallery()
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

    private var latestTmpUri: Uri? = null

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

    private fun getByteArrayFromBitmap(bitmap: Bitmap): ByteArray? {
        val bos = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, bos)
        return bos.toByteArray()
    }
}