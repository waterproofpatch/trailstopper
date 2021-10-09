package com.example.trailstopper;

import androidx.appcompat.app.AppCompatActivity;

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

    private ArrayList<Stock> stocks;
    private TextView textViewOutput;
    private Button buttonMakeRequest;
    private EditText editTextTicker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.stocks = new ArrayList<>();

        // discover the UI components
        this.initUiElements();

        // register listeners with the GUI elements
        this.registerListeners();
    }

    private void initUiElements() {
        this.textViewOutput = (TextView) findViewById(R.id.volleyResponseTextView);
        this.buttonMakeRequest = (Button)findViewById(R.id.buttonMakeRequest);
        this.editTextTicker = (EditText) findViewById(R.id.editTextTicker);

        this.textViewOutput.setMovementMethod(new ScrollingMovementMethod());
    }

    private void registerListeners() {
        this.buttonMakeRequest.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        makeRequest(editTextTicker.getText().toString().toLowerCase().trim(), textViewOutput);
                    }
                }
        );
    }

    private void updateView() {
        for (Stock s:stocks) {
            this.textViewOutput.setText(s.getRaw());
        }
    }

    private void setError(String error) {
        this.textViewOutput.setText("Error: " + error);
    }

    private void makeRequest(final String ticker, final TextView textView) {

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
                textView.setText("That didn't work! " + error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
