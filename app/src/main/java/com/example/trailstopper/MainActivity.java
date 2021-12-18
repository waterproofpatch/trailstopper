package com.example.trailstopper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private StockAdapter stockAdapter;
    private ArrayList<Stock> stocks;
    private Button addStockButton;
    private EditText stockTickerEditText;
    private SharedPreferences sharedPreferences;
    private final String sharedPrefsTickersKey = "tickers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // new backing for the recycler view
        this.stocks = new ArrayList<>();

        // discover the UI components
        this.discoverUIElements();

        // register listeners with the GUI elements
        this.registerListeners();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    // see https://www.vogella.com/tutorials/AndroidActionBar/article.html
    // see https://developer.android.com/guide/topics/ui/menus#xml
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_clear:
                Toast.makeText(this, "Removing all stocks...", Toast.LENGTH_SHORT)
                        .show();
                this.stocks.clear();
                this.updateView();
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                // opening a new intent to open settings activity.
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
            default:
                Log.i("onOptionsItemSelected", "not sure: " + item.getItemId());
                break;
        }

        return true;
    }

    @Override
    public void onStop() {
        saveStockTickers();
        super.onStop();
    }

    @Override
    public void onResume() {
        this.restoreStockTickers();
        super.onResume();
    }

    public void saveStockTickers() {
        Log.i("savePrefs", "pausing");
        sharedPreferences = getSharedPreferences(getLocalClassName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> tickerSet = new HashSet<>();
        for (Stock s: this.stocks) {
            s.stopUpdates();
            tickerSet.add(s.getTicker());
        }
        editor.putStringSet(sharedPrefsTickersKey, tickerSet);
        editor.apply();
    }

    /**
     * Restore preferences.
     */
    public void restoreStockTickers() {
        Log.i("restorePrefs", "restoring state, " + this.stocks.size() + " stocks");
        sharedPreferences = getSharedPreferences(getLocalClassName(), MODE_PRIVATE);
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("restoreStockTickers", "stockRefreshRate: " + defaultSharedPreferences.getString("stock_refresh_rate", "default"));

        Set<String> tickers = sharedPreferences.getStringSet(sharedPrefsTickersKey, null);
        if (tickers == null) {
            Log.i("restorePrefs", "tickers is null, no tickers to restore");
            return;
        }

        if (tickers.size() != this.stocks.size()) {
            Log.i("restorePrefs", "Tickers and stocks different lengths, re-adding all tickers");
            for (String ticker: tickers) {
                this.addNewStock(ticker);
            }
        } else {
            // existing stocks, just restart the updates
            for (Stock s : this.stocks) {
                s.startUpdates();
            }
        }
    }

    /**
     * Discover the UI elements.
     */
    private void discoverUIElements() {
        // discover the UI elements and save them off
        this.addStockButton = (Button)findViewById(R.id.buttonMakeRequest);
        this.stockTickerEditText = (EditText) findViewById(R.id.editTextTicker);
        RecyclerView stockRecyclerView = findViewById(R.id.idRecyclerViewStock);

        // we are initializing our adapter class and passing our arraylist to it.
        this.stockAdapter = new StockAdapter(this, this.stocks);

        // below line is for setting a layout manager for our recycler view.
        // here we are creating vertical list so we will provide orientation as vertical
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        // in below two lines we are setting LayoutManager and adapter to our recycler view.
        stockRecyclerView.setLayoutManager(linearLayoutManager);
        stockRecyclerView.setAdapter(this.stockAdapter);
    }

    /**
     * Register input listeners.
     */
    private void registerListeners() {
        this.addStockButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String ticker = stockTickerEditText.getText().toString().toLowerCase().trim();
                        if (!stockExists(ticker)) {
                            addNewStock(ticker);
                        } else {
                            setError("stock " + ticker + " exists!");
                        }
                    }
                }
        );
    }

    /**
     * Check if a stock exists.
     * @param ticker to check.
     * @return true if @c ticker exists.
     * @return false if @c ticker does not exist.
     */
    private boolean stockExists(String ticker) {
        for (Stock s : this.stocks) {
            if (s.getTicker().compareToIgnoreCase(ticker) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the stock adapter at @c position.
     * @param position in the stockAdapter to update.
     */
    public void updateView(int position) {
        if (stockAdapter != null) {
            stockAdapter.notifyItemChanged(position);
        }
        else {
            setError("stockAdapter is null!");
        }
    }
    public void updateView() {
        stockAdapter.notifyDataSetChanged();
    }

    /**
     * Display an error to the user.
     * @param error the error to display.
     */
    public void setError(String error) {
        Log.e("setError", error);
        ErrorDialogFragment frag = new ErrorDialogFragment();
        frag.setMessage(error);
        frag.show(getFragmentManager(), "setError");
    }

    /**
     * Add a new stock given a ticker.
     * @param ticker for the stock to add.
     */
    private void addNewStock(final String ticker) {
        Stock stock = new Stock(ticker, this, this.stocks);
        stocks.add(stock);
        stock.startUpdates();
    }
}
