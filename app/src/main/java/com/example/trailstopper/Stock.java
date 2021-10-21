package com.example.trailstopper;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

public class Stock {
    private String ticker;
    private String raw;
    private double price;
    private String longName;
    private String averageDailyVolume3Month;
    private String regularMarketPreviousClose;
    private double atr;
    private double trailStop;
    private double trailStopPct;

    // Instantiate the RequestQueue.

    public static String getCurrentDayUrl(String ticker) {
        return "https://finance.yahoo.com/quote/" + ticker;
    }

    public static String getTechnicalUrl(String ticker) {
        return "https://www.chartmill.com/chartmill-rest/screener/?sort=taRating&sorting=DESC&tickers="+ticker+"&start=0";
    }

    public static JSONObject parseCurrentDayAttributes(String content) throws JSONException {
        String json_piece = content.split("root.App.main =")[1].split("\\(this\\)")[0].split("\\;\\n\\}")[0].trim();

        JSONObject jObject = new JSONObject(json_piece);
        JSONObject obj = jObject.getJSONObject("context").getJSONObject("dispatcher").getJSONObject("stores").getJSONObject("QuoteSummaryStore");
        return obj;
    }

    public String getAtr() {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", atr);
        return formatter.toString();
    }

    public double getPrice() {
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

    public String getTicker() {
        return ticker;
    }

    public String getTrailStop() {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", trailStop);
        return formatter.toString();
    }

    public String getTrailStopPct() {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", trailStopPct);
        return formatter.toString();
    }

    public void calculateCurrentDayAttributes(JSONObject stockObject) throws JSONException {
        this.ticker = stockObject.getString("symbol");
        this.raw = stockObject.toString();
        this.regularMarketPreviousClose = stockObject.getJSONObject("price").getJSONObject("regularMarketPreviousClose").getString("fmt");
        this.longName = stockObject.getJSONObject("price").getString("longName");
        this.averageDailyVolume3Month = stockObject.getJSONObject("price").getJSONObject("averageDailyVolume3Month").getString("longFmt");
    }

    public void calculateTrailStop(JSONObject stockObject) throws JSONException {
        this.atr = stockObject.getJSONArray("result").getJSONObject(0).getJSONObject("technicals").getDouble("atr");
        this.price = stockObject.getJSONArray("result").getJSONObject(0).getJSONObject("technicals").getDouble("close");
        double atrp = (this.atr / this.price) * 100.0;
        this.trailStopPct = atrp * 2.5;
        this.trailStop = this.price - (this.price * (this.trailStopPct/100.0));
    }

    public Stock(String ticker) {
        this.ticker = ticker;
    }
}
