package com.example.trailstopper;


import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private final JSONObject stockObject;

    public Stock(JSONObject stockObject) {
        this.stockObject = stockObject;
    }

    public String getTicker() {
        try {
            return this.stockObject.getString("symbol");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
