package com.example.digi_dexproject;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.List;

@Entity(tableName = "cards")
@TypeConverters(Converters.class)
public class CardEntity {

    @PrimaryKey
    public int id;

    public String name;

    public String type;

    public List<String> typeline;

    @ColumnInfo(name = "description")
    public String desc;

    public String race;

    public Integer atk;

    public Integer def;

    public Integer level;

    public String attribute;



    @ColumnInfo(name = "image_url")
    public String imageUrl;

    @ColumnInfo(name = "image_url_small")
    public String imageUrlSmall;

    public String scannedByUser;

    @ColumnInfo(name = "card_prices")
    public List<CardPrice> cardPrices;

    @ColumnInfo(name = "last_updated")
    public long lastUpdated; // We'll store the timestamp in millisecond
}
