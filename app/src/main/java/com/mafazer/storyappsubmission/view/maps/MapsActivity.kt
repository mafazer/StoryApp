package com.mafazer.storyappsubmission.view.maps

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.mafazer.storyappsubmission.R
import com.mafazer.storyappsubmission.databinding.ActivityMapsBinding
import com.mafazer.storyappsubmission.data.Result
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.MapStyleOptions
import com.mafazer.storyappsubmission.view.ViewModelFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val boundsBuilder = LatLngBounds.Builder()
    private val viewModel: MapsViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        mMap.setOnMyLocationButtonClickListener {
            checkLocationEnabled()
            true
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            getUserLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            animateToDefaultLocation()
        }

        setMapStyle()
        getStoriesWithLocation()
    }

    private fun getUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLatLng = LatLng(location.latitude, location.longitude)
                        val bounds = LatLngBounds.builder()
                            .include(userLatLng)
                            .include(LatLng(location.latitude + 0.05, location.longitude + 0.05))
                            .include(LatLng(location.latitude - 0.05, location.longitude - 0.05))
                            .build()
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                    } else {
                        animateToDefaultLocation()
                    }
                }
                .addOnFailureListener {
                    animateToDefaultLocation()
                }
        }
    }

    private fun getStoriesWithLocation() {
        viewModel.getStoriesWithLocation().observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.errorTextView.visibility = View.GONE
                }

                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val stories = result.data.listStory
                    stories.forEach { story ->
                        val lat = story.lat
                        val lon = story.lon
                        if (lat != null && lon != null) {
                            val latLng = LatLng(lat, lon)
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(story.name)
                                    .snippet(story.description)
                            )
                            boundsBuilder.include(latLng)
                        }
                    }
                    animateToShowAllMarkers()
                }

                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = getString(R.string.error_message)
                }
            }
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private fun animateToShowAllMarkers() {
        try {
            val bounds: LatLngBounds = boundsBuilder.build()
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 100)
            )
        } catch (e: IllegalStateException) {
            animateToDefaultLocation()
        }
    }

    private fun animateToDefaultLocation() {
        val defaultLocation = LatLng(-2.8126584, 108.6067792)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
    }

    private fun checkLocationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        ) {
            showLocationEnableDialog()
        } else {
            getUserLocation()
        }
    }

    private fun showLocationEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enable_loc_title))
            .setMessage(getString(R.string.enable_loc_message))
            .setPositiveButton(getString(R.string.enable_loc)) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton(getString(R.string.cancel_loc), null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap.isMyLocationEnabled = true
                    getUserLocation()
                }
            } else {
                animateToDefaultLocation()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val TAG = "MapsActivity"
    }
}