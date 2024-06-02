package com.mafazer.storyappsubmission.view.addstory

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.mafazer.storyappsubmission.R
import com.mafazer.storyappsubmission.data.Result
import com.mafazer.storyappsubmission.databinding.ActivityAddStoryBinding
import com.mafazer.storyappsubmission.view.ViewModelFactory
import com.mafazer.storyappsubmission.view.reduceFileImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddStoryActivity : AppCompatActivity() {
    private val viewModel: AddStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var progressBar: ProgressBar

    private var currentPhotoPath: String? = null
    private var selectedPhotoFile: File? = null
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()

        progressBar = binding.progressBar

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.addStory)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupAction() {
        binding.btnCamera.setOnClickListener {
            requestPermissions()
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnUpload.setOnClickListener {
            uploadStory()
        }

        binding.switchUseLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!isLocationEnabled()) {
                    showLocationEnableDialog()
                    binding.switchUseLocation.isChecked = false
                } else {
                    getLocation()
                }
            } else {
                location = null
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)?.let {
            createTempFile(application).also { file ->
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "com.mafazer.storyappsubmission.fileprovider",
                    file
                )
                currentPhotoPath = file.absolutePath
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherIntentCamera.launch(intent)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath)
            binding.ivPreview.setImageURI(Uri.fromFile(file))
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data
            selectedImg?.let { uri ->
                val contentResolver = applicationContext.contentResolver
                val file = createTempFile(applicationContext.toString())

                val inputStream = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()

                selectedPhotoFile = file
                binding.ivPreview.setImageURI(uri)
            }
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString().trim()
        val photoFile = if (currentPhotoPath != null) {
            reduceFileImage(File(currentPhotoPath!!))
        } else {
            selectedPhotoFile?.let { reduceFileImage(it) }
        }

        if (photoFile == null) {
            Toast.makeText(this, "Silakan pilih foto terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val requestImageFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart = MultipartBody.Part.createFormData(
            "photo",
            photoFile.name,
            requestImageFile
        )
        val descriptionRequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

        val lat = if (binding.switchUseLocation.isChecked && location != null) {
            location?.latitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        } else {
            null
        }
        val lon = if (binding.switchUseLocation.isChecked && location != null) {
            location?.longitude?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        } else {
            null
        }

        viewModel.uploadStory(imageMultipart, descriptionRequestBody, lat, lon)
        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }

                is Result.Success -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Story berhasil diunggah", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }

                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Gagal mengunggah story: ${result.error}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun getLocation() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val providers = locationManager.getProviders(true)
            var location: Location? = null
            for (provider in providers) {
                location = locationManager.getLastKnownLocation(provider)
                if (location != null) {
                    break
                }
            }
            if (location != null) {
                this@AddStoryActivity.location = location
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun showLocationEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.enable_loc_title))
            .setMessage(getString(R.string.enable_loc_message))
            .setPositiveButton(getString(R.string.enable_loc)) { dialog, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel_loc)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        val permissionsNotGranted =
            permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }

        if (permissionsNotGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNotGranted.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun createTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(Date())


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

}