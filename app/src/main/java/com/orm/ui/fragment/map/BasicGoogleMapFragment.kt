package com.orm.ui.fragment.map

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.orm.R
import com.orm.data.model.Point
import com.orm.databinding.FragmentGoogleMapBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BasicGoogleMapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentGoogleMapBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var polyline: Polyline? = null
    private var points: List<Point> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            points = it.getParcelableArrayList(ARG_TRAIL) ?: emptyList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGoogleMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isMapToolbarEnabled = false
        }
        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(36.38, 127.51), 7f
            )
        )
        googleMap?.setOnCameraMoveListener {
            binding.root.parent.requestDisallowInterceptTouchEvent(true)
        }
        googleMap?.setOnMapClickListener {
            binding.root.parent.requestDisallowInterceptTouchEvent(true)
        }

        binding.map.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                binding.root.parent.requestDisallowInterceptTouchEvent(true)
            }
            false
        }

        updateMap(points)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun updateMap(points: List<Point>) {
        googleMap?.clear()
        googleMap?.let { map ->
            val polylinePoints = points.map { LatLng(it.x, it.y) }
            polyline?.remove()
            polyline = map.addPolyline(
                PolylineOptions()
                    .addAll(polylinePoints)
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

            if (polylinePoints.isNotEmpty()) {
                val startMarker = MarkerOptions()
                    .position(polylinePoints.first())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title("종점")

                val endMarker = MarkerOptions()
                    .position(polylinePoints.last())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .title("시점")

                map.addMarker(startMarker)
                map.addMarker(endMarker)

                val boundsBuilder = LatLngBounds.Builder()
                polylinePoints.map {
                    boundsBuilder.include(it)
                }

                map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(),
                        150
                    )
                )
            }
        } ?: Log.e(TAG, "GoogleMap is not initialized")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        googleMap = null
        _binding = null
    }

    fun updatePoints(newPoints: List<Point>) {
        updateMap(newPoints)
    }

    companion object {
        private const val TAG = "GoogleMapFragment"
        private const val ARG_TRAIL = "trail"

        fun newInstance(points: List<Point>): BasicGoogleMapFragment {
            val fragment = BasicGoogleMapFragment()
            val args = Bundle().apply {
                putParcelableArrayList(ARG_TRAIL, ArrayList(points))
            }
            fragment.arguments = args
            return fragment
        }
    }
}
