package com.wrongcode.captionwizard.ui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsetsController
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wrongcode.captionwizard.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var addVideoFab : FloatingActionButton
    private lateinit var toolBar : MaterialToolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navhost) as NavHostFragment
        addVideoFab = findViewById(R.id.addVideoFab)
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.videosFragment,
            R.id.subtitlesFragment,
        ))
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNavigationView.setupWithNavController(navController)
        toolBar.setupWithNavController(navController,appBarConfiguration)
        addVideoFab.setOnClickListener {
            checkStoragePermission()
        }

    }


    private fun checkStoragePermission(){
        val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else{
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if(permissionsToRequest.isNotEmpty()){
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
        else{
            openBottomSheet()
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){permissions ->
        val deniedPermission = permissions.entries.filter { !it.value }
        if(deniedPermission.isEmpty()){
            openBottomSheet()
        }
        else{
            val permanentlyDeniedPermissions = deniedPermission.filter { entry ->
                !ActivityCompat.shouldShowRequestPermissionRationale(this, entry.key)
            }.map { it.key }
            if(permanentlyDeniedPermissions.isNotEmpty()){
                onPermissionsPermanentlyDenied()
            }
            else{
                Toast.makeText(this, "Storage permissions denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPermissionsPermanentlyDenied() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Permissions")
            .setMessage("Storage permissions permanently denied")
            .setCancelable(false)
            .setPositiveButton("Go to Settings"){_, _ ->
                showSettingsDialog()
            }
            .setNegativeButton("Cancel"){dialogInterface, _ ->
                dialogInterface.cancel()
            }
        alertDialog.create().show()
    }

    private fun showSettingsDialog() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun openBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetStyle)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet)
        bottomSheetDialog.show()

        val importTextView : TextView? = bottomSheetDialog.findViewById(R.id.importTextView)
        importTextView?.setOnClickListener {
            openFilePicker()
            bottomSheetDialog.dismiss()
        }
    }

    private val resultIntent : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            data?.let {
                val uri = it.data
                uri?.let {itUri->
                    contentResolver.takePersistableUriPermission(itUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val intent = Intent(this@MainActivity, ProcessVideoActivity::class.java)
                    intent.putExtra("uri", itUri.toString())
                    startActivity(intent)
                }
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("video/*")
        resultIntent.launch(intent)
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() ||  super.onSupportNavigateUp()
    }
}