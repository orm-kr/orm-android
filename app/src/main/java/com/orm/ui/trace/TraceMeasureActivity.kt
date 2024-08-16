package com.orm.ui.trace

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import com.orm.data.model.Trace
import com.orm.databinding.ActivityTraceMeasureBinding
import com.orm.ui.fragment.map.TraceGoogleMapFragment
import com.orm.viewmodel.TrailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TraceMeasureActivity : AppCompatActivity() {

    private val binding: ActivityTraceMeasureBinding by lazy {
        ActivityTraceMeasureBinding.inflate(layoutInflater)
    }

    private val trace: Trace? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("trace", Trace::class.java)
        } else {
            intent.getParcelableExtra<Trace>("trace")
        }
    }

    private val trailViewModel: TrailViewModel by viewModels()

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            checkLocationSettingsAndSetupFragment()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (isLocationPermissionGranted()) {
            checkLocationSettingsAndSetupFragment()
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationSettingsAndSetupFragment() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            setupFragment()
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        showEnableGpsDialog()
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        showGpsUnavailableDialog()
                    }
                }
            }
        }
    }

    private fun setupFragment() {
        if (supportFragmentManager.findFragmentById(binding.fcvMap.id) == null) {
            val fragment = trace?.let { trace ->
                if (trace.trailId != null) {
                    trailViewModel.getTrail(trace.trailId)
                    var trailFragment: TraceGoogleMapFragment? = null
                    trailViewModel.trail.observe(this) { trail ->
                        trailFragment = TraceGoogleMapFragment.newInstance(
                            points = trail.trailDetails,
                            traceId = trace.localId
                        )
                        trailFragment?.let {
                            supportFragmentManager.beginTransaction()
                                .replace(binding.fcvMap.id, it)
                                .commit()
                        }
                        trailViewModel.trail.removeObservers(this)
                    }
                    return
                } else {
                    TraceGoogleMapFragment.newInstance(emptyList(), trace.localId)
                }
            } ?: TraceGoogleMapFragment.newInstance(emptyList())

            fragment.let {
                supportFragmentManager.beginTransaction()
                    .replace(binding.fcvMap.id, it)
                    .commit()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("위치 권한 필요")
            .setMessage("이 기능을 사용하려면 위치 권한이 필요합니다. 설정에서 권한을 허용해 주세요.")
            .setPositiveButton("설정으로 이동") { dialog, _ ->
                dialog.dismiss()
                val intent =
                    Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
                    }
                startActivity(intent)
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .setOnDismissListener {
                onBackPressedDispatcher.onBackPressed()
            }
            .show()
    }

    private fun showEnableGpsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("GPS 설정 필요")
            .setMessage("이 기능을 사용하려면 GPS가 필요합니다. GPS를 활성화해 주세요.")
            .setPositiveButton("설정으로 이동") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
                onBackPressedDispatcher.onBackPressed()
            }
            .setOnDismissListener {
                onBackPressedDispatcher.onBackPressed()
            }
            .show()
    }

    private fun showGpsUnavailableDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("GPS 설정 부족")
            .setMessage("GPS 설정이 충분하지 않습니다. GPS 설정을 확인해 주세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
