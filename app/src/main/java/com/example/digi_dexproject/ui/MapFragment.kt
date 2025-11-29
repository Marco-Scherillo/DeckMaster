package com.example.digi_dexproject.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digi_dexproject.BuildConfig
import com.example.digi_dexproject.MainActivity
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            locationPermissionGranted = true
            updateLocationUI()
            getDeviceLocation()
        } else {
            locationPermissionGranted = false
            updateLocationUI()
            Toast.makeText(context, "Location permission denied. Map features will be limited.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (BuildConfig.MAPS_API_KEY.isEmpty()) {
            Toast.makeText(requireContext(), "MAPS_API_KEY not found", Toast.LENGTH_LONG).show()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            return
        }


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
        if (!locationPermissionGranted) {
            getLocationPermission() // Re-prompt for permission
            return // Do not proceed
        }

        if (!isNetworkAvailable()) {
            loadPlacesFromCache()
            return
        }

        try {
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
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        getLocationPermission()

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
        val lastKnownLocation = currentLocation ?: return

        placesAdapter = PlacesAdapter(places, lastKnownLocation) { place ->
            showPlaceDetails(place)
            place.latLng?.let {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                placesRecyclerView.visibility = View.GONE
            }
        }
        placesRecyclerView.adapter = placesAdapter
        placesRecyclerView.visibility = View.VISIBLE

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
        val serializablePlaces = places.mapNotNull { place ->
            if (place.name != null && place.address != null && place.latLng != null) {
                 SerializablePlace(place.name!!, place.address!!, place.latLng!!)
            } else null
        }
        val gson = Gson()
        val json = gson.toJson(serializablePlaces)
        placesCacheFile.writeText(json)
    }

    private fun loadPlacesFromCache() {
        if (placesCacheFile.exists() && locationPermissionGranted) {
            val gson = Gson()
            val json = placesCacheFile.readText()
            val type = object : TypeToken<List<SerializablePlace>>() {}.type
            val serializablePlaces: List<SerializablePlace> = gson.fromJson(json, type)

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
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                locationPermissionGranted = true
                updateLocationUI()
                getDeviceLocation()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                Toast.makeText(context, "Location permission is required to find nearby stores.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                mMap.clear()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception in updateLocationUI", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (!::mMap.isInitialized || !locationPermissionGranted) return
        try {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
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
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception in getDeviceLocation", e)
        }
    }

    companion object {
        private const val TAG = "MapFragment"
    }
}
