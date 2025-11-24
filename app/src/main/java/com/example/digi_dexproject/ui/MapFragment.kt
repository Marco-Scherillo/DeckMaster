package com.example.digi_dexproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digi_dexproject.BuildConfig
import com.example.digi_dexproject.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var searchView: SearchView
    private lateinit var placesRecyclerView: RecyclerView
    private lateinit var placesAdapter: PlacesAdapter
    private lateinit var placeDetailCard: CardView

    private val defaultLocation = LatLng(40.7357, -74.1724) // Newark, NJ
    private var currentLocation: Location? = null
    private var locationPermissionGranted = false

    private val placesCacheFile by lazy {
        File(requireContext().cacheDir, "places_cache.json")
    }

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

        val cardGameShopsChip = view.findViewById<Chip>(R.id.chip_card_game_shops)
        cardGameShopsChip.setOnClickListener {
            runSearchWithCurrentLocation("card game shops")
        }

        placesRecyclerView = view.findViewById(R.id.places_recycler_view)
        placesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        placeDetailCard = view.findViewById(R.id.place_detail_card)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    runSearchWithCurrentLocation(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun runSearchWithCurrentLocation(query: String) {
        if (!isNetworkAvailable()) {
            loadPlacesFromCache()
            return
        }

        if (locationPermissionGranted) {
            try {
                // Suppress lint warning, as we are handling the exception.
                @SuppressLint("MissingPermission")
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationProviderClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )
                    .addOnSuccessListener { location: Location? ->
                        val searchLocation = if (location != null) {
                            currentLocation = location
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                            currentLatLng
                        } else {
                            mMap.cameraPosition.target
                        }
                        searchForPlaces(query, searchLocation)
                    }.addOnFailureListener {
                        loadPlacesFromCache()
                    }
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission has been revoked.", e)
                loadPlacesFromCache()
            }
        } else {
            loadPlacesFromCache()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()

        mMap.setOnMarkerClickListener { marker ->
            marker.tag?.let { showPlaceDetails(it as Place) }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.position))
            marker.showInfoWindow()
            true
        }

        mMap.setOnMapClickListener {
            placesRecyclerView.visibility = View.GONE
            placeDetailCard.visibility = View.GONE
        }
    }

    private fun searchForPlaces(query: String, location: LatLng) {
        val placeFields = listOf(
            Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS,
            Place.Field.PHOTO_METADATAS
        )
        val searchBounds = RectangularBounds.newInstance(
            LatLng(location.latitude - 0.1, location.longitude - 0.1),
            LatLng(location.latitude + 0.1, location.longitude + 0.1)
        )
        val searchRequest = SearchByTextRequest.builder(query, placeFields)
            .setLocationBias(searchBounds)
            .build()

        placesClient.searchByText(searchRequest)
            .addOnSuccessListener { response ->
                mMap.clear()
                Log.i(TAG, "Found ${response.places.size} places for query: $query")
                if (response.places.isNotEmpty()) {
                    savePlacesToCache(response.places)
                    displayPlaces(response.places)
                } else {
                    Log.i(TAG, "No results found for query: $query")
                    placesRecyclerView.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error searching for places: ", exception)
                loadPlacesFromCache()
            }
    }

    private fun displayPlaces(places: List<Place>) {
        currentLocation?.let {
            placesAdapter = PlacesAdapter(places, it) { place ->
                showPlaceDetails(place)
                place.latLng?.let {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                    placesRecyclerView.visibility = View.GONE
                }
            }
            placesRecyclerView.adapter = placesAdapter
            placesRecyclerView.visibility = View.VISIBLE
        }
        for (place in places) {
            place.latLng?.let { latLng ->
                val marker = mMap.addMarker(
                    MarkerOptions()
                        .title(place.name)
                        .position(latLng)
                        .snippet(place.address)
                )
                marker?.tag = place
            }
        }
    }

    private fun savePlacesToCache(places: List<Place>) {
        val serializablePlaces = places.map {
            SerializablePlace(it.name, it.address, it.latLng)
        }
        val gson = Gson()
        val json = gson.toJson(serializablePlaces)
        placesCacheFile.writeText(json)
    }

    private fun loadPlacesFromCache() {
        if (placesCacheFile.exists()) {
            val gson = Gson()
            val json = placesCacheFile.readText()
            val type = object : TypeToken<List<SerializablePlace>>() {}.type
            val serializablePlaces = gson.fromJson<List<SerializablePlace>>(json, type)

            val places = serializablePlaces.map {
                Place.builder()
                    .setName(it.name)
                    .setAddress(it.address)
                    .setLatLng(it.latLng)
                    .build()
            }
            if (places.isNotEmpty()) {
                displayPlaces(places)
            }
        }
    }


    private fun showPlaceDetails(place: Place) {
        val placeNameDetail: TextView = placeDetailCard.findViewById(R.id.place_name_detail)
        val placeAddressDetail: TextView = placeDetailCard.findViewById(R.id.place_address_detail)
        val placeImage: ImageView = placeDetailCard.findViewById(R.id.place_image)

        placeNameDetail.text = place.name
        placeAddressDetail.text = place.address

        val photoMetadata = place.photoMetadatas?.firstOrNull()
        if (photoMetadata != null && isNetworkAvailable()) {
            val photoRequest = FetchPhotoRequest.builder(photoMetadata).build()
            placesClient.fetchPhoto(photoRequest)
                .addOnSuccessListener { fetchPhotoResponse ->
                    val bitmap: Bitmap = fetchPhotoResponse.bitmap
                    placeImage.setImageBitmap(bitmap)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Photo not found for place: ${place.name}", exception)
                }
        } else {
            placeImage.setImageDrawable(null)
        }
        placeDetailCard.visibility = View.VISIBLE
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
                Log.d(TAG, "Fetching current location with balanced power accuracy.")
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationProviderClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationTokenSource.token
                )
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            currentLocation = location
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                            Log.d(TAG, "Successfully fetched current location: $currentLatLng")
                        } else {
                            Log.d(TAG, "Failed to get current location. Using defaults.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Exception while getting location", e)
                    }
            } else {
                Log.d(TAG, "Location permission not granted. Using defaults.")
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
