package com.orm.ui.fragment.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.orm.R
import com.orm.data.model.Point
import com.orm.data.model.Record
import com.orm.databinding.FragmentTraceGoogleMapBinding
import com.orm.ui.trace.TraceActivity
import com.orm.util.LocationIntentService
import com.orm.util.localDateTimeToLong
import com.orm.viewmodel.RecordViewModel
import com.orm.viewmodel.TraceViewModel
import com.orm.viewmodel.TrackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow
import android.content.BroadcastReceiver as LocalReceiver

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class TraceGoogleMapFragment : Fragment(), OnMapReadyCallback, SensorEventListener {

    private var _binding: FragmentTraceGoogleMapBinding? = null
    private val binding get() = _binding!!

    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(requireContext())
    }

    private val trackViewModel: TrackViewModel by viewModels()
    private val recordViewModel: RecordViewModel by viewModels()
    private val traceViewModel: TraceViewModel by viewModels()


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var pressureSensor: Sensor
    private lateinit var updateTimeRunnable: Runnable

    private var saveRecordId: Int? = null
    private var googleMap: GoogleMap? = null
    private var points: List<Point> = emptyList()
    private var polyline: Polyline? = null
    private var userPolyline: Polyline? = null
    private var currentHeight: Double? = null
    private var maxTrackHeight: Double? = null
    private var traceId: Int? = null

    private val handler = Handler(Looper.getMainLooper())
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
    private val timeZone = TimeZone.getTimeZone("")
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var running = false

    private var tempFlag = false

    private val locationReceiver = object : LocalReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0

            Log.d("LocationReceiver", "Received location: $latitude, $longitude")
            if (latitude == 0.0 && longitude == 0.0) {
                tempFlag = true
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("위치 정보를 가져올 수 없습니다.")
                    .setMessage("GPS가 켜져있는지 확인해주세요.")
                    .setPositiveButton("확인") { _, _ ->
                    }
                    .show()
                return
            } else {
                if (tempFlag) {
                    tempFlag = false
                    Toast.makeText(requireContext(), "위치 정보를 다시 가져옵니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                updateMapWithLocation(LatLng(latitude, longitude))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeServices()

        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) ?: run {
            Log.e(TAG, "Pressure sensor not available")
            return
        }

        arguments?.let {
            points = it.getParcelableArrayList(ARG_POINT) ?: emptyList()
            traceId = it.getInt(ARG_TRACE_ID)
        }

        dateFormat.timeZone = timeZone
        updateTimeRunnable = object : Runnable {
            override fun run() {
                updateCurrentTime()
                handler.postDelayed(this, 1000)
            }
        }

        recordViewModel.getRecordCount()
        recordViewModel.recordId.observe(this) {
            saveRecordId = it.toInt() + 1
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTraceGoogleMapBinding.inflate(inflater, container, false)

        binding.btnStop.visibility = View.GONE
        binding.distance = "0m"

        binding.btnStart.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("측정 시작")
                .setMessage("발자국 측정을 시작하시겠습니까?")
                .setPositiveButton("확인") { _, _ ->
                    binding.btnStart.visibility = View.GONE
                    binding.btnStop.visibility = View.VISIBLE
                    startLocationService()
                    startStopwatch()
                    Toast.makeText(requireContext(), "발자국 측정을 시작합니다.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소") { _, _ ->
                }
                .show()
        }

        binding.btnStop.setOnClickListener {
            stopTrace()
        }

        return binding.root
    }

    private fun stopTrace() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("측정 종료")
            .setMessage("정말로 종료하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                trackViewModel.points.value?.let { points ->
                    insertRecordAndHandleTrace(points, true)
                }
                stopLocationService()

                traceViewModel.traceCreatedNormal.removeObservers(viewLifecycleOwner)
                traceViewModel.traceCreatedNormal.observe(viewLifecycleOwner) { isCreated ->
                    if (isCreated) {
                        val intent = Intent(requireContext(), TraceActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
            .setNegativeButton("취소") { _, _ ->
            }
            .show()
    }

    private fun insertRecordAndHandleTrace(points: List<Point>, isFinished: Boolean = false) {
        // 비동기 작업을 위해 IO 스레드에서 실행
        CoroutineScope(Dispatchers.IO).launch {
            // 데이터베이스 작업 또는 다른 IO 작업 수행
            recordViewModel.insertRecord(
                Record(
                    id = saveRecordId!!,
                    coordinate = points
                )
            )

            if (traceId != null && traceId != -1) {
                traceViewModel.getTrace(traceId!!)

                withContext(Dispatchers.Main) {
                    traceViewModel.trace.observe(viewLifecycleOwner) { trace ->
                        trace?.let {
                            // 비동기 작업을 위해 IO 스레드에서 실행
                            CoroutineScope(Dispatchers.IO).launch {
                                traceViewModel.createTrace(it.apply {
                                    recordId = saveRecordId!!.toLong()
                                    hikingStartedAt = startTime
                                    hikingEndedAt = System.currentTimeMillis()
                                    maxHeight = maxTrackHeight
                                    hikingDistance = trackViewModel.distance.value
                                })

                                withContext(Dispatchers.Main) {
                                    traceViewModel.traceCreated.removeObservers(viewLifecycleOwner)
                                    traceViewModel.traceCreated.observe(viewLifecycleOwner) { isCreated ->
                                        if (isCreated && isFinished) {
                                            Toast.makeText(
                                                requireContext(),
                                                "발자국 측정이 완료되었습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun initializeServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                Log.d("gmap", "Location fetched: $location")
                location?.let {
                    updateMapWithLocation(LatLng(it.latitude, it.longitude))
                } ?: run {
                    Log.e("gmap", "Location not available")
                }
            }
            .addOnFailureListener { e ->
                Log.e("gmap", "Failed to get location", e)
            }
    }

    private fun startLocationService() {
        val intent = Intent(requireContext(), LocationIntentService::class.java)
        requireContext().startService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(requireContext(), LocationIntentService::class.java)
        requireContext().stopService(intent)
    }

    private fun updateMapWithLocation(latlng: LatLng) {
        googleMap?.let { map ->
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(latlng, map.cameraPosition.zoom)
            )

            if (!running) return

            trackViewModel.updatePoint(
                Point(
                    x = latlng.latitude,
                    y = latlng.longitude,
                    altitude = currentHeight,
                    time = localDateTimeToLong(LocalDateTime.now())
                )
            )

            trackViewModel.distance.observe(requireActivity()) {
                binding.distance = String.format(Locale.KOREA, "%.0f", it) + "m"
            }

            binding.altitude = String.format(Locale.KOREA, "%.0f", currentHeight) + "m"

            trackViewModel.points.observe(requireActivity()) {
                val positions = it.map { pos ->
                    LatLng(pos.x, pos.y)
                }

                userPolyline?.remove()
                userPolyline = map.addPolyline(
                    PolylineOptions()
                        .addAll(positions)
                        .color(Color.HSVToColor(floatArrayOf(30f, 1f, 1f)))
                        .startCap(RoundCap())
                        .endCap(RoundCap())
                        .jointType(JointType.ROUND)
                        .pattern(
                            listOf(
                                Dot(),
                                Gap(10f),
                                Dash(30f),
                                Gap(10f)
                            )
                        )
                        .clickable(true)
                )
            }


            if (!trackViewModel.points.value.isNullOrEmpty()
                && trackViewModel.points.value!!.size % 3 == 0
                && trackViewModel.points.value!!.size >= 3
            ) {
                CoroutineScope(Dispatchers.IO).launch {

                    insertRecordAndHandleTrace(trackViewModel.points.value!!)
                }
            }
        } ?: Log.e(TAG, "GoogleMap is not initialized")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        localBroadcastManager.registerReceiver(
            locationReceiver, IntentFilter("com.orm.LOCATION_UPDATE"),
        )

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!running) {
                        requireActivity().finish()
                    } else {
                        stopTrace()
                    }
                }
            })
    }

    private fun isLocationPermissionGranted(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (isLocationPermissionGranted()) {
            googleMap?.isMyLocationEnabled = true
        } else {
            startActivity(
                Intent(requireContext(), TraceActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }

        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap?.moveCamera(CameraUpdateFactory.zoomTo(18f))

        fetchLocation()
        initializeMap(points)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_PRESSURE) {
                currentHeight = calculateAltitude(it.values[0])
                binding.altitude = String.format(Locale.KOREA, "%.0f", currentHeight) + "m"
                maxTrackHeight = maxOf(maxTrackHeight ?: 0.0, currentHeight ?: 0.0)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Implement accuracy changes if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationService()
        localBroadcastManager.unregisterReceiver(locationReceiver)
        handler.removeCallbacks(updateTimeRunnable)
        traceViewModel.traceCreated.removeObservers(viewLifecycleOwner)
        googleMap = null
        _binding = null
    }

    private fun calculateAltitude(pressure: Float): Double =
        44330 * (1 - (pressure / 1013.25).pow(1 / 5.255))

    private fun updateCurrentTime() {
        if (running) {
            val currentTime = System.currentTimeMillis()
            val timeInMillis = elapsedTime + (currentTime - startTime)
            binding.time = dateFormat.format(Date(timeInMillis))
        }
    }

    private fun startStopwatch() {
        if (!running) {
            running = true
            startTime = System.currentTimeMillis()
            updateTimeRunnable.run()
        }
    }

    private fun initializeMap(points: List<Point>) {
        googleMap?.let { map ->
            val latLngPoints = points.map { LatLng(it.x, it.y) }

            polyline?.remove()
            polyline = map.addPolyline(
                PolylineOptions()
                    .addAll(latLngPoints)
                    .color(Color.RED)
                    .startCap(RoundCap())
                    .endCap(RoundCap())
                    .jointType(JointType.ROUND)
                    .pattern(
                        listOf(
                            Dot(),
                            Gap(10f),
                            Dash(30f),
                            Gap(10f)
                        )
                    )
                    .clickable(true)
            )
        } ?: Log.e(TAG, "GoogleMap is not initialized")
    }

    companion object {
        private const val TAG = "GoogleMapTraceFragment"
        private const val ARG_POINT = "point"
        private const val ARG_TRACE_ID = "trace_id"

        fun newInstance(points: List<Point>, traceId: Int? = null): TraceGoogleMapFragment {
            val fragment = TraceGoogleMapFragment()
            val args = Bundle().apply {
                putParcelableArrayList(ARG_POINT, ArrayList(points))
                putInt(ARG_TRACE_ID, traceId ?: -1)
            }
            fragment.arguments = args
            return fragment
        }
    }
}

