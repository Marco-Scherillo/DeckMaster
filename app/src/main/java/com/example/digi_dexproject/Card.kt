package com.example.digi_dexproject

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Card(
    val name: String,
    val level: Int,
    val type: String,
    val attribute: String,
    val race: String,
    val atk: Int,
    val def: Int,
    val desc: String,
    val card_images: List<CardImage>
) : Parcelable

@Parcelize
data class CardImage(
    @SerializedName("id")
    val id: Int,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("image_url_small")
    val imageUrlSmall: String,
    @SerializedName("image_url_cropped")
    val imageUrlCropped: String?
) : Parcelable