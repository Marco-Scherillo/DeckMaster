package com.example.digi_dexproject;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CardData {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("type")
    private String type;

    @SerializedName("typeline")
    private List<String> typeline;

    @SerializedName("desc")
    private String desc;

    @SerializedName("race")
    private String race;

    @SerializedName("atk")
    private int atk;

    @SerializedName("def")
    private int def;

    @SerializedName("level")
    private int level;

    @SerializedName("attribute")
    private String attribute;

    @SerializedName("card_images")
    private List<CardImage> cardImages;

    @SerializedName("card_prices")
    private List<CardPrice> cardPrices;

    public List<CardPrice> getCardPrices() {return cardPrices;}



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getTypeline() {
        return typeline;
    }

    public String getDesc() {
        return desc;
    }

    public String getRace() {
        return race;
    }

    public int getAtk() {
        return atk;
    }

    public int getDef() {
        return def;
    }

    public int getLevel() {
        return level;
    }

    public String getAttribute() {
        return attribute;
    }



    public List<CardImage> getCardImages() {
        return cardImages;
    }

}
