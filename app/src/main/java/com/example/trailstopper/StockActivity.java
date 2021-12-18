package com.example.trailstopper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

public class StockActivity extends AppCompatActivity {

    private String ticker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        // initialize the stock data display
        Bundle b = getIntent().getExtras();
        if (b != null) {
            this.ticker = b.getString("ticker");
            Log.i("onCreate", "using ticker " + this.ticker);
            TextView tickerTextView = findViewById(R.id.stockTickerTextView);
            tickerTextView.setText(this.ticker);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                Log.i("onOptionsItemSelected", "not sure which option: " + item.getItemId());
                break;
        }
        return true;
    }
}