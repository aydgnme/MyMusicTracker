package me.aydgn.mymusictracker

import LoginActivity
import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        setContentView(R.layout.activity_main)

        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isSeeded = sharedPref.getBoolean("isSeeded", false)

        if (!isSeeded) {
            // Seed işlemi tamamlandıktan sonra bayrağı ayarla
            sharedPref.edit().putBoolean("isSeeded", true).apply()
        }


        firebaseAuth = FirebaseAuth.getInstance()


    }

    private fun checkLocationPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(this, "Location permissions are granted", Toast.LENGTH_SHORT).show()
            }

            else -> {
                // Request permissions
                requestLocationPermissions()
            }
        }
    }

    private fun requestLocationPermissions() {
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                ) {
                    // Permissions granted
                    Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Permissions denied
                    Toast.makeText(this, "Location permissions denied", Toast.LENGTH_SHORT).show()
                }
            }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Method to be called when the login button is clicked
    fun onLoginClick(view: android.view.View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    // Method to be called when the register button is clicked
    fun onRegisterClick(view: android.view.View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

}