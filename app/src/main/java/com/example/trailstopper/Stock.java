package com.example.trailstopper;


import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private final JSONObject stockObject;
    private final String ticker;
    private final String raw;

    public Stock(JSONObject stockObject) throws JSONException {
        this.stockObject = stockObject;
        this.ticker = this.stockObject.getString("symbol");
        this.raw = this.stockObject.toString();

    }

    public String getRaw() {
        return this.raw;
    }

    public String getTicker() {
        return this.ticker;
    }

}
