package com.example.digi_dexproject;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardApiResponse {

    @SerializedName("data")
    private List<CardData> data;

    public List<CardData> getData() {
        return data;
    }
}
