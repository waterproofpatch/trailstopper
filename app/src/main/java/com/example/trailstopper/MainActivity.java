package com.example.trailstopper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView stockRecyclerView;
    private StockAdapter stockAdapter;
    private ArrayList<Stock> stocks;
    private TextView textViewOutput;
    private Button buttonMakeRequest;
    private EditText editTextTicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stockRecyclerView = findViewById(R.id.idRecyclerViewStock);

        this.stocks = new ArrayList<>();

        // we are initializing our adapter class and passing our arraylist to it.
        stockAdapter = new StockAdapter(this, stocks);

        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting layoutmanager and adapter to our recycler view.
        stockRecyclerView.setLayoutManager(linearLayoutManager);
        stockRecyclerView.setAdapter(stockAdapter);

        makeRequest("MSFT");

        // discover the UI components
        //this.initUiElements();

        // register listeners with the GUI elements
        //this.registerListeners();
    }

    private void initUiElements() {
        // discover the UI elements and save them off
        //this.textViewOutput = (TextView) findViewById(R.id.volleyResponseTextView);
        //this.buttonMakeRequest = (Button)findViewById(R.id.buttonMakeRequest);
        //this.editTextTicker = (EditText) findViewById(R.id.editTextTicker);

        // update behaviors
        this.textViewOutput.setMovementMethod(new ScrollingMovementMethod());
    }

    private void registerListeners() {
        this.buttonMakeRequest.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeRequest(editTextTicker.getText().toString().toLowerCase().trim());
                    }
                }
        );
    }

    private void updateView() {
        if (stockAdapter != null) {
            stockAdapter.notifyDataSetChanged();
        }
        else {
            Log.e("updateView", "stockAdapter is null!");
        }

        if (this.textViewOutput == null) {
            Log.e("updateView", "not updating because we have a null textViewOutout");
            return;
        }
        for (Stock s:stocks) {
            this.textViewOutput.setText(s.getLongName() + " - " + s.getPrice());
        }
    }

    private void setError(String error) {
        this.textViewOutput.setText("Error: " + error);
    }

    private void makeRequest(final String ticker) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://finance.yahoo.com/quote/" + ticker;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject obj = null;
                        try {
                            obj = ResponseJsonParser.parseIntoJson(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setError("Failed parsing stock data for " + ticker + ": " + e.getMessage());
                            return;
                        }
                        Stock stock = null;
                        try {
                            stock = new Stock(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            setError("Failed constructing stock data for " + ticker + ": " + e.getMessage());
                            return;
                        }
                        stocks.add(stock);
                        updateView();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("onErrorResponse", "That didn't work: " + error.toString());
                setError("That didn't work! " + error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
