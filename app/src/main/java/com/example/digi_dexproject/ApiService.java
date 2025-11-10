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
            @Query("num") int num,
            @Query("offset") int offset
    );
}
