package com.example.trailstopper;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Stock {
    private String ticker;
    private String raw;
    private String price;
    private String longName;
    private String averageDailyVolume3Month;
    private String regularMarketPreviousClose;

    static JSONObject parseCurrentDayAttributes(String content) throws JSONException {
        String json_piece = content.split("root.App.main =")[1].split("\\(this\\)")[0].split("\\;\\n\\}")[0].trim();

        JSONObject jObject = new JSONObject(json_piece);
        JSONObject obj = jObject.getJSONObject("context").getJSONObject("dispatcher").getJSONObject("stores").getJSONObject("QuoteSummaryStore");
        return obj;
    }

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

    public void getCurrentDayAttributes(JSONObject stockObject) throws JSONException {
        this.ticker = stockObject.getString("symbol");
        this.raw = stockObject.toString();
        this.price = stockObject.getJSONObject("price").getJSONObject("regularMarketPrice").getString("fmt");
        this.regularMarketPreviousClose = stockObject.getJSONObject("price").getJSONObject("regularMarketPreviousClose").getString("fmt");
        this.longName = stockObject.getJSONObject("price").getString("longName");
        this.averageDailyVolume3Month = stockObject.getJSONObject("price").getJSONObject("averageDailyVolume3Month").getString("longFmt");
    }

    public void getFiveDayAttributes(JSONObject stockObject) throws JSONException {
        JSONArray result = stockObject.getJSONObject("chart").getJSONArray("result");
        JSONObject indicators = result.getJSONObject(0).getJSONObject("indicators");
        JSONObject fiveDayDict = indicators.getJSONArray("quote").getJSONObject(0);
        JSONArray closingArray = fiveDayDict.getJSONArray("close");
        JSONArray volumeArray = fiveDayDict.getJSONArray("volume");
        Log.i("getFiveDayAttributes", "ok");
    }

    public Stock(String ticker) {
        this.ticker = ticker;
      }
}
