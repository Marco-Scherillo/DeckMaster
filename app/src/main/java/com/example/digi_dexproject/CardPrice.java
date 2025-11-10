package com.example.digi_dexproject;

import com.google.gson.annotations.SerializedName;

public class CardPrice {

    @SerializedName("cardmarket_price")
    private String cardmarketPrice;

    @SerializedName("tcgplayer_price")
    private String tcgplayerPrice;

    @SerializedName("ebay_price")
    private String ebayPrice;

    @SerializedName("amazon_price")
    private String amazonPrice;

    @SerializedName("coolstuffinc_price")
    private String coolstuffincPrice;

    public String getCardmarketPrice() {
        return cardmarketPrice;
    }

    public String getTcgplayerPrice() {
        return tcgplayerPrice;
    }

    public String getEbayPrice() {
        return ebayPrice;
    }

    public String getAmazonPrice() {
        return amazonPrice;
    }

    public String getCoolstuffincPrice() {
        return coolstuffincPrice;
    }
}
