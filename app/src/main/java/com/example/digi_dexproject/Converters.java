package com.example.digi_dexproject;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class Converters {
    private static Gson gson = new Gson();

    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        return gson.toJson(list);
    }
    @TypeConverter
    public static String fromCardPriceList(List<CardPrice> cardPrices) {
        if (cardPrices == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(cardPrices);
    }

    @TypeConverter
    public static List<CardPrice> toCardPriceList(String data) {
        if (data == null) {
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<CardPrice>>() {}.getType();
        return gson.fromJson(data, listType);
    }
}
