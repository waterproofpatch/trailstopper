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

        Bundle b = getIntent().getExtras();
        this.ticker = b.getString("ticker");

        TextView tickerTextView = findViewById(R.id.tickerTextView);
        tickerTextView.setText(this.ticker);

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