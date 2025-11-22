package com.example.digi_dexproject.ui

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class SerializablePlace(
    val name: String?,
    val address: String?,
    val latLng: LatLng?
) : Serializable