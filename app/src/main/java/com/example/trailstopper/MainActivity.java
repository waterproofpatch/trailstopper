package com.example.trailstopper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView stockRecyclerView;
    private StockAdapter stockAdapter;
    private ArrayList<Stock> stocks;
    private Button buttonMakeRequest;
    private EditText editTextTicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // in below two lines we are setting layoutmanager and adapter to our recycler view.
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
                            makeRequest(ticker);
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
    }

    private void makeRequest(final String ticker) {

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.start();

        // Instantiate the RequestQueue.
        String urlCurrentDay = "https://finance.yahoo.com/quote/" + ticker;
        String url5day = "https://query1.finance.yahoo.com/v8/finance/chart/"+ticker+"?region=US&lang=en-US&includePrePost=false&interval=1d&useYfid=true&range=5d&corsDomain=finance.yahoo.com&.tsrc=finance";
        final int numRequests = 2;
        final JSONObject[] jsonObjects = new JSONObject[numRequests];
        final CountDownLatch countdownLatch = new CountDownLatch(numRequests);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlCurrentDay,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("stringRequest", "got response!");
                        try {
                            jsonObjects[0] = ResponseJsonParser.parseIntoJson(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setError("Failed parsing stock data for " + ticker + ": " + e.getMessage());
                            return;
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                setError("That didn't work! " + error.toString());
            }
        });

        // 5 day request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url5day, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("jsonObjectRequest", "got response!");
                        jsonObjects[1] = response;
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setError("That didn't work! " + error.toString());
                    }
                });

        // Add the requests to the RequestQueue.
        queue.add(stringRequest);
        queue.add(jsonObjectRequest);

        queue.addRequestEventListener(new RequestQueue.RequestEventListener() {
            @Override
            public void onRequestEvent(Request<?> request, int event) {
                if (event == RequestQueue.RequestEvent.REQUEST_FINISHED) {
                    Log.i("onRequestEvent", "Request " + request + " is finished");
                    countdownLatch.countDown();

                    if (countdownLatch.getCount() == 0) {
                        // create the stock object
                        try {
                            Stock stock = new Stock(jsonObjects[0]);
                            stocks.add(stock);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setError("Failed constructing stock data for " + ticker + ": " + e.getMessage());
                            return;
                        }

                        // update the view now that we have a new stock object
                        updateView();
                    }
                }
            }
        });

        queue.start();
        }
}
