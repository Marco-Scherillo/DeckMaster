package com.example.digi_dexproject.ui

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.digi_dexproject.R
import com.google.android.libraries.places.api.model.Place

class PlacesAdapter(
    private var places: List<Place>,
    private val currentLocation: Location,
    private val onPlaceClickListener: (Place) -> Unit
) : RecyclerView.Adapter<PlacesAdapter.PlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.bind(place)
    }

    override fun getItemCount(): Int = places.size

    fun updatePlaces(newPlaces: List<Place>) {
        places = newPlaces
        notifyDataSetChanged()
    }

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.place_name)
        private val distanceTextView: TextView = itemView.findViewById(R.id.place_distance)

        fun bind(place: Place) {
            nameTextView.text = place.name

            place.latLng?.let {
                val placeLocation = Location("").apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
                val distance = currentLocation.distanceTo(placeLocation) / 1000 // in kilometers
                distanceTextView.text = String.format("%.2f km", distance)
            }

            itemView.setOnClickListener {
                onPlaceClickListener(place)
            }
        }
    }
}