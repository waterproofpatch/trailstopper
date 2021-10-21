package com.example.trailstopper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView stockRecyclerView;
    private StockAdapter stockAdapter;
    private ArrayList<Stock> stocks;
    private Button buttonMakeRequest;
    private EditText editTextTicker;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        // new backing for the recycler view
        this.stocks = new ArrayList<>();

        // discover the UI components
        this.initUiElements();

        // register listeners with the GUI elements
        this.registerListeners();
    }

    private void initUiElements() {
        // discover the UI elements and save them off
        this.buttonMakeRequest = (Button)findViewById(R.id.buttonMakeRequest);
        this.editTextTicker = (EditText) findViewById(R.id.editTextTicker);
        this.stockRecyclerView = findViewById(R.id.idRecyclerViewStock);

        // we are initializing our adapter class and passing our arraylist to it.
        this.stockAdapter = new StockAdapter(this, this.stocks);

        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting LayoutManager and adapter to our recycler view.
        this.stockRecyclerView.setLayoutManager(linearLayoutManager);
        this.stockRecyclerView.setAdapter(this.stockAdapter);
    }

    private void registerListeners() {
        this.buttonMakeRequest.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String ticker = editTextTicker.getText().toString().toLowerCase().trim();
                        if (stockExists(ticker) == false) {
                            makeRequests(ticker);
                        } else {
                            setError("stock " + ticker + " exists!");
                        }
                    }
                }
        );
    }

    private boolean stockExists(String ticker) {
        for (Stock s : this.stocks) {
            if (s.getTicker().compareToIgnoreCase(ticker) == 0) {
                return true;
            }
        }
        return false;
    }

    private void updateView() {
        if (stockAdapter != null) {
            stockAdapter.notifyDataSetChanged();
        }
        else {
            setError("stockAdapter is null!");
        }
    }

    private void setError(String error) {
        Log.e("setError", error);
        ErrorDialogFragment frag = new ErrorDialogFragment();
        frag.setMessage(error);
        frag.show(getFragmentManager(), "setError");
    }

    private void makeRequests(final String ticker) {
        final Stock stock = new Stock(ticker);
        stocks.add(stock);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Stock.getCurrentDayUrl(ticker),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("stringRequest", "got response!");
                        try {
                            stock.calculateCurrentDayAttributes(Stock.parseCurrentDayAttributes(response));
                            updateView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setError("Failed parsing current stock data for " + ticker + ": " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setError("Error with volley: " + error.toString());
            }
        });

        // N day request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, Stock.getTechnicalUrl(ticker), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("jsonObjectRequest", "got response!");
                        try {
                            stock.calculateTrailStop(response);
                            updateView();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setError("Failed parsing five day stock data for " + ticker + ": " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setError("Error with volley: " + error.toString());
                    }
                }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Referer", "https://www.chartmill.com/stock/quote/"+ticker+"/technical=analysis");
                return headers;
            }};

        // Add the requests to the RequestQueue.
        queue.add(stringRequest);
        queue.add(jsonObjectRequest);

        }
}
