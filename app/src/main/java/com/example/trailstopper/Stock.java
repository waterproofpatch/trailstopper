package com.example.trailstopper;


import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private final JSONObject stockObject;
    private final String ticker;
    private final String raw;
    private final String price;
    private final String longName;
    private final String averageDailyVolume3Month;
    private final String regularMarketPreviousClose;

    public String getPrice() {
        return price;
    }

    public String getLongName() {
        return longName;
    }

    public String getAverageDailyVolume3Month() {
        return averageDailyVolume3Month;
    }

    public String getRegularMarketPreviousClose() {
        return regularMarketPreviousClose;
    }

    public String getRaw() {
        return raw;
    }

    public String getTicker() {
        return ticker;
    }
    public Stock(JSONObject stockObject) throws JSONException {
        this.stockObject = stockObject;
        this.ticker = this.stockObject.getString("symbol");
        this.raw = this.stockObject.toString();
        this.price = this.stockObject.getJSONObject("price").getJSONObject("regularMarketPrice").getString("fmt");
        this.regularMarketPreviousClose = this.stockObject.getJSONObject("price").getJSONObject("regularMarketPreviousClose").getString("fmt");
        this.longName = this.stockObject.getJSONObject("price").getString("longName");
        this.averageDailyVolume3Month = this.stockObject.getJSONObject("price").getJSONObject("averageDailyVolume3Month").getString("longFmt");
    }


}
