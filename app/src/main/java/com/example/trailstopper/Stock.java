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
    private String price;
    private String longName;
    private String averageDailyVolume3Month;
    private String regularMarketPreviousClose;
    private double atr;
    private double trailStop;


    private double trailStopPct;

    static JSONObject parseCurrentDayAttributes(String content) throws JSONException {
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

    public void getCurrentDayAttributes(JSONObject stockObject) throws JSONException {
        this.ticker = stockObject.getString("symbol");
        this.raw = stockObject.toString();
        this.price = stockObject.getJSONObject("price").getJSONObject("regularMarketPrice").getString("fmt");
        this.regularMarketPreviousClose = stockObject.getJSONObject("price").getJSONObject("regularMarketPreviousClose").getString("fmt");
        this.longName = stockObject.getJSONObject("price").getString("longName");
        this.averageDailyVolume3Month = stockObject.getJSONObject("price").getJSONObject("averageDailyVolume3Month").getString("longFmt");
    }

    public void calculateTrailStop(JSONObject stockObject) throws JSONException, StockParsingException {
        JSONArray result = stockObject.getJSONObject("chart").getJSONArray("result");
        JSONObject indicators = result.getJSONObject(0).getJSONObject("indicators");
        JSONObject fiveDayDict = indicators.getJSONArray("quote").getJSONObject(0);

        JSONArray closingArray = fiveDayDict.getJSONArray("close");
        JSONArray highArray = fiveDayDict.getJSONArray("high");
        JSONArray lowArray = fiveDayDict.getJSONArray("low");
        JSONArray volumeArray = fiveDayDict.getJSONArray("volume");

        if (closingArray.length() < 15 ||
        highArray.length() < 15 ||
        lowArray.length() < 15) {
            Log.e("getFiveDayAttributes" , "Arrays not long enough!");
            throw new StockParsingException("Array lengths are not big enough!");
        }

        /* The true range indicator is taken as the greatest of the following:
        current high less the current low; the absolute value of the current high less the previous
        close; and the absolute value of the current low less the previous close. The ATR is then a
        moving average, generally using 14 days, of the true ranges. */
        ArrayList<Double> trueRanges = new ArrayList<>();
        double prevAtr = 0.0;
        for (int day = 0; day < 14; day++) {
            double curHigh = highArray.getDouble(day);
            double curLow = lowArray.getDouble(day);
            double prevClose = 0.0;
            if (day > 0) {
                prevClose = closingArray.getDouble(day-1);
            }

            // first calculation
            double calc1 = curHigh - curLow;
            double calc2 = 0.0;
            if (prevClose > 0.0) {
                calc2 = Math.abs(curHigh - prevClose);
            }
            double calc3 = 0.0;
            if (prevClose > 0.0) {
                calc3 = Math.abs(curLow - prevClose);
            }

            double trueRange = Math.max(calc3, Math.max(calc1, calc2));
            Log.i("calculateTrailStop", "calc1: " + calc1 + ", calc2: " + calc2 +", calc3: " + calc3 + ", trueRange: " + trueRange);
            trueRanges.add(trueRange);

            // WMAi = WMAi-1 + (Pricei - WMAi-1) / N
            prevAtr = (prevAtr + (closingArray.getDouble(day) - prevAtr)) / 14.0;
        }

        // now that we have each day's TR, we calculate the moving average therein
        double totalRange = 0;
        for (Double trueRange : trueRanges) {
            totalRange += trueRange;
        }

        this.atr = totalRange / (double)trueRanges.size();
        //this.atr = prevAtr;
        Log.i("calculateTrailStop", "ATR is " + this.atr);

        this.trailStop = Double.valueOf(closingArray.get(13).toString()).doubleValue() - (2.5 * this.atr);
        this.trailStopPct = 100.0 - (this.trailStop / Double.valueOf(closingArray.get(13).toString()).doubleValue()) * 100.0;
    }

    public Stock(String ticker) {
        this.ticker = ticker;
    }
}
