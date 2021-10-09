package com.example.trailstopper;


import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private String ticker;
    private String raw;
    private String price;
    private String longName;
    private String averageDailyVolume3Month;
    private String regularMarketPreviousClose;

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

    private void getAttributes(JSONObject stockObject) throws JSONException {
        this.ticker = stockObject.getString("symbol");
        this.raw = stockObject.toString();
        this.price = stockObject.getJSONObject("price").getJSONObject("regularMarketPrice").getString("fmt");
        this.regularMarketPreviousClose = stockObject.getJSONObject("price").getJSONObject("regularMarketPreviousClose").getString("fmt");
        this.longName = stockObject.getJSONObject("price").getString("longName");
        this.averageDailyVolume3Month = stockObject.getJSONObject("price").getJSONObject("averageDailyVolume3Month").getString("longFmt");
    }

    public Stock(JSONObject stockObject) throws JSONException {
        this.getAttributes(stockObject);
      }
}
