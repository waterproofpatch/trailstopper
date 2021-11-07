package com.example.trailstopper;


import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Stock {
    private String ticker;
    private double price;
    private String longName;
    private String averageDailyVolume3Month;
    private String regularMarketPreviousClose;
    private double atr;
    private double trailStop;
    private double trailStopPct;
    private MainActivity parentActivity;
    private ArrayList<Stock> stockList;
    private Timer timer;

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

    private void update(final int position) {
        Log.i("update","Updating " + this.ticker + " position " + position);
        final Stock _this = this;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Stock.getCurrentDayUrl(ticker),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("stringRequest", "got response!");
                        try {
                            _this.calculateCurrentDayAttributes(Stock.parseCurrentDayAttributes(response));
                            _this.parentActivity.updateView(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _this.parentActivity.setError("Failed parsing current stock data for " + _this.ticker + ": " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                _this.parentActivity.setError("Error with volley: " + error.toString());
            }
        });

        // N day request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Stock.getTechnicalUrl(ticker), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("jsonObjectRequest", "got response!");
                        try {
                            _this.calculateTrailStop(response);
                            _this.parentActivity.updateView(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            _this.parentActivity.setError("Failed parsing five day stock data for " + _this.ticker + ": " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        _this.parentActivity.setError("Error with volley: " + error.toString());
                    }
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Referer", "https://www.chartmill.com/stock/quote/"+_this.ticker+"/technical=analysis");
                return headers;
            }};

        // Add the requests to the RequestQueue.
        RequestQueueSingleton.getInstance(this.parentActivity).addToRequestQueue(stringRequest);
        RequestQueueSingleton.getInstance(this.parentActivity).addToRequestQueue(jsonObjectRequest);
    }

    public void startUpdates() {
        final Stock _this = this;
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (_this.stockList.indexOf(_this) == -1) {
                    Log.w("startUpdates", _this.ticker + " - looks like I've been removed");
                    this.cancel();
                } else {
                    _this.update(_this.stockList.indexOf(_this));
                }
            }
        }, 0, 10 * 1000);
    }

    public void stopUpdates() {
        if (this.timer != null) {
            Log.i("stopUpdates", this.getTicker() + " stopping updates");
            this.timer.cancel();
        }
    }

    public Stock(String ticker, MainActivity parentActivity, ArrayList<Stock> stockList) {
        this.ticker = ticker;
        this.parentActivity = parentActivity;
        this.stockList = stockList;
    }
}
