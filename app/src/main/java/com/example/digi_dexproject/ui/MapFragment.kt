package com.example.digi_dexproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
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
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val defaultLocation = LatLng(-33.8523341, 151.2106085) // Sydney
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
            Places.initialize(requireContext(), getString(R.string.MAPS_API_KEY))
        }
        placesClient = Places.createClient(requireContext())

        // Initialize the FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Get the SupportMapFragment and request the map asynchronously.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    private fun searchNearbyStores(location: LatLng) {
        // This is a good place to implement a more specific search.
        // The current implementation uses findCurrentPlace, which is broad.
        // To find "Hobby Stores" or "Card Shops", you'll want to use a Text Search.
        // For now, we will refine the existing search.
        searchForHobbyStores(location)
    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            // Use the fragment's requestPermissions method for cleaner results handling.
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
                // Permission granted, now get the location and update the UI.
                updateLocationUI()
                getDeviceLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (!::mMap.isInitialized) return // Check if mMap has been initialized

        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true // This enables the blue dot for user location
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                // No need to call getLocationPermission() here, it creates a loop.
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
                        searchNearbyStores(currentLocation)
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception getting location: ", task.exception)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception in getDeviceLocation", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun searchForHobbyStores(location: LatLng) {
        // Define the fields you want to get for each place.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.TYPES)
        val request = FindCurrentPlaceRequest.newInstance(placeFields)

        val placeResult = placesClient.findCurrentPlace(request)
        placeResult.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val likelyPlaces = task.result

                mMap.clear() // Clear previous markers

                for (placeLikelihood in likelyPlaces.placeLikelihoods) {
                    val place = placeLikelihood.place
                    val placeTypes = place.types
                    val placeName = place.name?.lowercase() ?: ""
                    if (placeTypes != null && (placeTypes.contains(Place.Type.STORE) || placeTypes.contains(Place.Type.BOOK_STORE) || placeName.contains("hobby"))) {
                        Log.i(TAG, "Found store: ${place.name} with types: ${place.types}")
                        place.latLng?.let { latLng ->
                            mMap.addMarker(
                                MarkerOptions()
                                    .title(place.name)
                                    .position(latLng)
                                    .snippet(place.address)
                            )
                        }
                    }
                }
            } else {
                Log.e(TAG, "Exception while fetching places: ", task.exception)
            }
        }
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }
}
