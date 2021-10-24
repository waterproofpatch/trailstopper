package com.example.trailstopper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView stockRecyclerView;
    private StockAdapter stockAdapter;
    private ArrayList<Stock> stocks;
    private Button buttonMakeRequest;
    private EditText editTextTicker;
    private SharedPreferences sharedPreferences;
    private final String sharedPrefsTickersKey = "tickers";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(getLocalClassName(), MODE_PRIVATE);



        // new backing for the recycler view
        this.stocks = new ArrayList<>();

        // discover the UI components
        this.initUiElements();

        // register listeners with the GUI elements
        this.registerListeners();

        // restore tickers
        this.restorePrefs();

    }

    @Override
    public void onPause() {
        Log.i("onPause", "pausing");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> tickerSet = new HashSet<>();
        for (Stock s: this.stocks) {
            tickerSet.add(s.getTicker());
        }
        editor.putStringSet(sharedPrefsTickersKey, tickerSet);
        editor.commit();

        // Always call the superclass so it can save the view hierarchy state
        super.onPause();
    }

    public void restorePrefs() {
        Log.i("loadPrefs", "restoring state");

        Set<String> tickers = sharedPreferences.getStringSet(sharedPrefsTickersKey, null);
        if (tickers == null) {
            Log.i("restorePrefs", "tickers is null, no tickers to restore");
            return;
        }
        for (String ticker: tickers) {
            this.addNewStock(ticker);
        }
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
                        if (!stockExists(ticker)) {
                            addNewStock(ticker);
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

    public void updateView(int position) {
        if (stockAdapter != null) {
            stockAdapter.notifyItemChanged(position);
        }
        else {
            setError("stockAdapter is null!");
        }
    }

    public void setError(String error) {
        Log.e("setError", error);
        ErrorDialogFragment frag = new ErrorDialogFragment();
        frag.setMessage(error);
        frag.show(getFragmentManager(), "setError");
    }

    private void addNewStock(final String ticker) {
        Stock stock = new Stock(ticker, this, this.stocks);
        stocks.add(stock);
        stock.startUpdates();
    }
}
