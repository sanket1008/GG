package com.unc.gearupvr.ui.visit_college_nc

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.CancelableCallback
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*
import com.unc.gearupvr.R
import com.unc.gearupvr.model.College


class CollegeMapFragment : CollegesListViewFragment(), GoogleMap.OnMyLocationButtonClickListener {


    companion object {
        fun newInstance(viewModelShared: VisitCollegeNCViewModel) =
            CollegeMapFragment().apply {
                this.viewModel = viewModelShared
            }

        private const val LOCATION_REQUEST = 5000
    }

    private var mapView: MapView? = null
    private lateinit var map: GoogleMap
    private var shouldListenToCameraUpdate: Boolean = false

    private fun getMarker(college: College, icon: BitmapDescriptor): Marker? {

        //Create map location from college
        val location =
            college.location?.latitude?.let { lat ->
                college.location.longitude?.let { lng ->
                    LatLng(
                        lat,
                        lng
                    )
                }
            }

        //annotate location on map view
        location?.let {
            val marker = map.addMarker(
                MarkerOptions().position(it).title(college.name).icon(icon)
            )
            marker?.tag = 0

            college.marker = marker
            return marker
        }
        return null
    }

    private lateinit var mapBound: LatLngBounds

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        adapter.isNavIconVisible = true
        binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
        val headerView =
            inflater.inflate(R.layout.google_map, null)
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        headerView?.layoutParams = layoutParams
        headerView.id = R.id.mapView_parent
        binding.parentLayout.addView(headerView)
        mapView = headerView?.findViewById(R.id.mapView) as MapView
        val swipeLayout = binding.swipeContainer
            .layoutParams as RelativeLayout.LayoutParams
        headerView.id.let { swipeLayout.addRule(RelativeLayout.BELOW, it) }


        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            //Mark colleges in map view
            val icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_place)
            viewModel?.collegeList?.observe(
                this,
                Observer { collegeItems ->
                    //googleMap.clear()
                    map.clear()
                    enableUserLocationButton()
                    map.setOnMyLocationButtonClickListener(this)
                    var camera: CameraUpdate? = null
                    if (collegeItems.size > 1) {
                        val mapBoundBuilder = LatLngBounds.builder()
                        collegeItems.forEach { college ->
                            val marker = getMarker(college, icon)
                            mapBoundBuilder.include(marker?.position)
                        }
                        try {
                            mapBound = mapBoundBuilder.build()
                            camera = CameraUpdateFactory.newLatLngBounds(mapBound, 50)
                        } catch (e: Exception) {
                            println(e.localizedMessage)
                        }

                    } else if (collegeItems.size == 1) {
                        val marker = getMarker(collegeItems.first(), icon)
                        camera = CameraUpdateFactory.newLatLng(marker?.position)
                    }
                    //Zoom map to make all marker visible
                    if (camera != null)
                        googleMap?.animateCamera(camera)
                })

            //Disable marker selection
            googleMap.setOnMarkerClickListener {
                true
            }


            //Handle map view zoom and scroll
            googleMap.setOnCameraMoveStartedListener { reason ->
                shouldListenToCameraUpdate =
                    reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
            }
            googleMap.setOnCameraIdleListener {
                if (shouldListenToCameraUpdate) {
                    shouldListenToCameraUpdate = false
                    println("northeast" + googleMap.projection.visibleRegion.latLngBounds.northeast)
                    println("southwest" + googleMap.projection.visibleRegion.latLngBounds.southwest)
                    locationValues = googleMap.projection.visibleRegion.latLngBounds
                    viewModel?.loadData(latLng = locationValues)
                }
            }
        }
        return view
    }

    override fun onStart() {
        if (mapView != null)
            mapView?.onStart()

        super.onStart()
    }


    override fun onResume() {
        if (mapView != null)
            mapView?.onResume()
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
        if (mapView != null)
            mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (mapView != null)
            mapView?.onStop()
    }

    override fun onDestroyView() {
        if (mapView != null)
            mapView?.onDestroy()
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mapView != null)
            mapView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }


    override fun onItemClick(
        newCollege: College,
        oldCollege: College?,
        oldItemView: View?,
        newItemView: View
    ) {
        if (oldCollege?.uid != newCollege.uid && (oldCollege?.marker?.tag != null && oldCollege.marker?.tag != 0)) {
            (oldCollege.marker)?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place))
            oldCollege.marker?.tag = 0
            context?.let {
                oldItemView?.setBackgroundColor(
                    ContextCompat.getColor(
                        it,
                        R.color.college_list_background_color
                    )
                )
            }
        }
        if (newCollege.marker?.tag != 0) {
            (newCollege.marker)?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place))
            newCollege.marker?.tag = 0
            context?.let {
                newItemView.setBackgroundColor(
                    ContextCompat.getColor(
                        it,
                        R.color.college_list_background_color
                    )
                )
            }
        } else {
            (newCollege.marker)?.let { marker ->
                (map).let { googleMap ->
                    if (!googleMap.projection.visibleRegion.latLngBounds.contains(newCollege.marker?.position)
                    ) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
                    }
                }
                marker.remove()
                newCollege.marker = map.addMarker(
                    MarkerOptions().position(marker.position)
                        .title(marker.title)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_place_selected))
                )
                newCollege.marker?.tag = 1
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 8f))
                context?.let {
                    newItemView.setBackgroundColor(
                        ContextCompat.getColor(
                            it,
                            R.color.college_list_selection_color
                        )
                    )
                }
            }
        }


    }

    override fun search(query: String) {
        viewModel?.query = query
        showLoader()
        viewModel?.loadData(latLng = locationValues)
    }

    override fun loadOnScroll() {
        if (viewModel?.loadData(
                loadMore = true,
                latLng = locationValues
            ) == true
        )
            binding.aviPagination.avi.smoothToShow()
    }

    private fun enableUserLocationButton() {
        try {
            if (canAccessLocation()) {
                map.isMyLocationEnabled = true
            } else {
                Log.d(
                    "LOCATION_REQUEST",
                    "don't have permission for location. requesting permission"
                )
                requestPermissions(
                    arrayOf(
                        ACCESS_COARSE_LOCATION
                    ), LOCATION_REQUEST
                )
            }
        } catch (e: java.lang.Exception) {
            Log.e(
                "LOCATION_REQUEST",
                e.localizedMessage ?: "Error while requesting location permission"
            )
        }

    }

    private fun canAccessLocation(): Boolean {
        context?.let {
            return (ContextCompat.checkSelfPermission(
                it,
                ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_REQUEST -> {
                if (canAccessLocation()) {
                    map.isMyLocationEnabled = true
                } else {
                    Log.d("LOCATION_REQUEST", "permission not granted")
                }
            }
            else -> {
                Log.d("PermissionsResult", "Unhandled permission request")
            }
        }

    }

    override fun onMyLocationButtonClick(): Boolean {

        //  This is method returns the last Known location and store it in location object from where then you can retrieve latitude and longitude.

        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it,
                    ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val locationManager =
                    activity?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val location =
                    locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                location?.let {
                    map.run {
                        animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    location.latitude,
                                    location.longitude
                                ), 8.0f
                            ),
                            object : CancelableCallback {
                                override fun onFinish() {
                                    locationValues = map.projection.visibleRegion.latLngBounds
                                    viewModel?.loadData(latLng = locationValues)
                                }

                                override fun onCancel() {

                                }
                            }
                        )
                    }
                }
            }
        }
        return true
    }
}