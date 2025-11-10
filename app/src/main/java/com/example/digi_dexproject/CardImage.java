package com.example.digi_dexproject;

import com.google.gson.annotations.SerializedName;

public class CardImage {

    @SerializedName("id")
    private int id;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("image_url_small")
    private String imageUrlSmall;

    @SerializedName("image_url_cropped")
    private String imageUrlCropped;

    public int getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageUrlSmall() {
        return imageUrlSmall;
    }

    public String getImageUrlCropped() {
        return imageUrlCropped;
    }
}
