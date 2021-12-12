package com.example.trailstopper;


import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

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
    private String longName;
    private MainActivity parentActivity;
    private ArrayList<Stock> stockList;
    private Timer timer;
    private double price;
    private double atr;
    private double trailStop;
    private double trailStopPct;

    public static String getUpdateUrl(String ticker) {
        return "https://www.chartmill.com/chartmill-rest/screener/?sort=taRating&sorting=DESC&tickers="+ticker+"&start=0";
    }

    public String getAtr() {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", this.atr);
        return formatter.toString();
    }

    public double getPrice() {
        return this.price;
    }

    public String getLongName() {
        return this.longName;
    }

    public String getTicker() {
        return this.ticker;
    }

    public String getTrailStop() {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", this.trailStop);
        return formatter.toString();
    }

    public String getTrailStopPct() {
        Formatter formatter = new Formatter();
        formatter.format("%.2f", this.trailStopPct);
        return formatter.toString();
    }

    public void calculateTrailStop(JSONObject stockObject) throws JSONException {
        this.atr = stockObject.getJSONArray("result").getJSONObject(0).getJSONObject("technicals").getDouble("atr");
        this.ticker = stockObject.getJSONArray("result").getJSONObject(0).getString("ticker");
        this.longName = stockObject.getJSONArray("result").getJSONObject(0).getString("name");
        this.price = stockObject.getJSONArray("result").getJSONObject(0).getJSONObject("technicals").getDouble("close");
        double atrp = (this.atr / this.price) * 100.0;
        this.trailStopPct = atrp * 2.5;
        this.trailStop = this.price - (this.price * (this.trailStopPct / 100.0));
    }

    private void update(final int position) {
        Log.i("update","Updating " + this.ticker + " position " + position);
        final Stock _this = this;

        // N day request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Stock.getUpdateUrl(ticker), null, new Response.Listener<JSONObject>() {
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
        RequestQueueSingleton.getInstance(this.parentActivity).addToRequestQueue(jsonObjectRequest);
    }

    /**
     * Start the update thread.
     */
    public void startUpdates() {
        final Stock _this = this;
        if (this.timer != null) {
            throw new RuntimeException("Timer already active!");
        }
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

    /**
     * Stop the update thread.
     */
    public void stopUpdates() {
        if (this.timer != null) {
            Log.i("stopUpdates", this.getTicker() + " stopping updates");
            this.timer.cancel();
        }
        this.timer = null;
    }

    public Stock(String ticker, MainActivity parentActivity, ArrayList<Stock> stockList) {
        this.ticker = ticker;
        this.parentActivity = parentActivity;
        this.stockList = stockList;
    }
}
