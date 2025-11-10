package com.example.digi_dexproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("cardinfo.php")
    Call<CardApiResponse> getCardData(
            @Query("startdate") String startDate,
            @Query("enddate") String endDate,
            @Query("dateregion") String dateRegion,
            @Query("num") Integer num,
            @Query("offset") Integer offset
    );

    @GET("cardinfo.php")
    Call<CardApiResponse> getSpecificCard(
            @Query("name") String name,
            @Query("startdate") String startDate,
            @Query("enddate") String endDate,
            @Query("dateregion") String dateRegion
    );

    @GET("cardinfo.php")
    Call<CardApiResponse> getCardsBySet(
            @Query("cardset") String cardset,
            @Query("dateregion") String dateRegion,
            @Query("startdate") String startDate,
            @Query("enddate") String endDate
    );
}
