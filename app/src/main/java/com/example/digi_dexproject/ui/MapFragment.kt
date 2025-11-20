package com.example.digi_dexproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView // <-- ADD THIS IMPORT
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.digi_dexproject.BuildConfig
import com.example.digi_dexproject.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var searchView: SearchView

    private val defaultLocation = LatLng(40.7357, -74.1724) // Newark, NJ
    private var locationPermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        }
        placesClient = Places.createClient(requireContext())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        searchView = view.findViewById(R.id.map_search_view)
        setupSearchView()

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }


    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // This method is called when the user presses the search button
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchForPlaces(query, mMap.cameraPosition.target)
                    searchView.clearFocus() // Hide the keyboard
                }
                return true
            }

            // This method is called for every character change in the search bar
            override fun onQueryTextChange(newText: String?): Boolean {
                // We don't need to do anything here, but it's required to implement.
                return false
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }



    private fun searchForPlaces(query: String, location: LatLng) {
        // 1. Define the fields to return for each place.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

        // 2. Define the search area (a box around the current map center).
        val searchBounds = RectangularBounds.newInstance(
            LatLng(location.latitude - 0.1, location.longitude - 0.1), // SW corner
            LatLng(location.latitude + 0.1, location.longitude + 0.1)  // NE corner
        )

        // 3. Build the search request using the user's query.
        val searchRequest = SearchByTextRequest.builder(query, placeFields)
            .setLocationBias(searchBounds) // Prefer results within our search area.
            .build()

        // 4. Execute the search.
        placesClient.searchByText(searchRequest)
            .addOnSuccessListener { response ->
                mMap.clear() // Clear old markers.
                Log.i(TAG, "Found ${response.places.size} places for query: $query")

                if (response.places.isNotEmpty()) {
                    // Add markers for all found places
                    for (place in response.places) {
                        place.latLng?.let { latLng ->
                            mMap.addMarker(
                                MarkerOptions()
                                    .title(place.name)
                                    .position(latLng)
                                    .snippet(place.address)
                            )
                        }
                    }
                    // Move camera to the first result
                    response.places.first().latLng?.let { firstResultLatLng ->
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstResultLatLng, 15f))
                    }
                } else {
                    Log.i(TAG, "No results found for query: $query")
                    // You could show a Toast message to the user here
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error searching for places: ", exception)
            }
    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true
                updateLocationUI()
                getDeviceLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (!::mMap.isInitialized) return
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception in updateLocationUI", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (!::mMap.isInitialized) return
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        val currentLocation = LatLng(task.result!!.latitude, task.result!!.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                        Log.d(TAG, "Current location: $currentLocation")
                        // We no longer search for hobby stores automatically
                        // searchNearbyStores(currentLocation)
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception in getDeviceLocation", e)
        }
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}