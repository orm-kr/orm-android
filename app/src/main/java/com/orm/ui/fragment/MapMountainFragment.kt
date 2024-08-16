package com.orm.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.orm.R
import com.orm.data.model.Mountain
import com.orm.databinding.FragmentMapMountainBinding
import com.orm.ui.mountain.MountainDetailActivity
import com.orm.util.NetworkUtils
import com.orm.viewmodel.MountainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapMountainFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapMountainBinding? = null
    private val binding get() = _binding!!

    private lateinit var clusterManager: ClusterManager<MyItem>
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var googleMap: GoogleMap? = null
    private var mountains: List<Mountain>? = null
    private val mountainMap = mutableMapOf<Int, Mountain>()

    private val mountainViewModel: MountainViewModel by viewModels()

    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            googleMap?.let { setupMap() }
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMapMountainBinding.inflate(inflater, container, false)

        mountainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMap()
        if (!isLocationPermissionGranted()) {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        googleMap = null
        _binding = null
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun setupMap() {
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

        val uiSettings = googleMap?.uiSettings
        uiSettings?.isCompassEnabled = true
        uiSettings?.isZoomControlsEnabled = true
        uiSettings?.isMapToolbarEnabled = false
        uiSettings?.isMyLocationButtonEnabled = isLocationPermissionGranted()

        googleMap?.isMyLocationEnabled = isLocationPermissionGranted()

        val southKoreaLatLng = LatLng(36.38, 127.51)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(southKoreaLatLng, 7f))
        googleMap?.setMinZoomPreference(5f)
        googleMap?.setMaxZoomPreference(15f)

        if (isLocationPermissionGranted()) {
            fetchLocation()
        }
        setUpClusterer()
        observeMountains()
    }

    private fun observeMountains() {
        mountainViewModel.fetchMountainsAll()
        mountainViewModel.mountains.observe(viewLifecycleOwner) { mountainList ->
            if (mountainList != null) {
                mountains = mountainList
                mountainMap.clear()
                mountains?.forEach { mountain ->
                    mountainMap[mountain.id] = mountain
                }
                addItems()
            }
        }
    }

    private fun setUpClusterer() {
        clusterManager = ClusterManager(context, googleMap)
        googleMap?.setOnCameraIdleListener(clusterManager)

        clusterManager.setOnClusterClickListener { cluster ->
            googleMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    cluster.position,
                    googleMap?.cameraPosition?.zoom?.plus(2) ?: 14f
                )
            )
            true
        }

        clusterManager.setOnClusterItemInfoWindowClickListener { item ->
            mountainViewModel.fetchMountainById(item.getId(), false)
            mountainViewModel.mountain.observe(viewLifecycleOwner) { mountain ->
                mountain?.let {
                    val intent = Intent(requireContext(), MountainDetailActivity::class.java)
                    intent.putExtra("mountain", it)
                    startActivity(intent)
                    mountainViewModel.clearMountainData()
                }
            }
        }
    }

    private fun addItems() {
        mountains?.forEach { mountain ->
            val offsetItem = MyItem(
                mountain.id,
                mountain.addressLatitude,
                mountain.addressLongitude,
                mountain.name,
                mountain.height.toString() + "m",
            )
            clusterManager.addItem(offsetItem)
        }
        clusterManager.cluster()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    googleMap?.let { map ->
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.latitude, it.longitude),
                                9.0f
                            )
                        )
                    }
                } ?: run {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("위치 정보 불러오기 실패")
                        .setMessage("위치 정보를 불러올 수 없습니다.")
                        .setPositiveButton("확인") { dialog, _ ->
                            dialog.dismiss()
                        }.show()

                    Log.e(TAG, "Location not available")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get location", e)
            }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("위치 권한 필요")
            .setMessage("현재 위치를 파악하기 위해서는 권한이 필요합니다. 설정에서 권한을 허용해 주세요.")
            .setPositiveButton("설정으로 이동") { dialog, _ ->
                dialog.dismiss()
                val intent =
                    Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data =
                            android.net.Uri.fromParts(
                                "package",
                                requireContext().packageName,
                                null
                            )
                    }
                startActivity(intent)
            }
            .show() // Removed the cancel button
    }

    inner class MyItem(
        private val id: Int,
        lat: Double,
        lng: Double,
        private val title: String,
        private val snippet: String,
    ) : ClusterItem {

        private val position: LatLng = LatLng(lat, lng)

        override fun getPosition(): LatLng = position
        override fun getTitle(): String = title
        override fun getSnippet(): String = snippet
        fun getId(): Int = id
    }

    companion object {
        private const val TAG = "MapMountainFragment"
    }
}
