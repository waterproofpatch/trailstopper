package com.example.trailstopper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.Viewholder> {

    private Context context;
    private ArrayList<Stock> stockArrayList;

    // Constructor
    public StockAdapter(Context context, ArrayList<Stock> stockArrayList) {
        this.context = context;
        this.stockArrayList = stockArrayList;
    }

    @NonNull
    @Override
    public StockAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        Stock stock = stockArrayList.get(position);
        if (stock == null) {
            return;
        }

        holder.ticker.setText(stock.getLongName());
        holder.price.setText("" + stock.getPrice());
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return stockArrayList.size();
    }

    // View holder class for initializing of
    // your views such as TextView and Imageview.
    public class Viewholder extends RecyclerView.ViewHolder {
        private TextView ticker;
        private TextView price;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.ticker);
            price = itemView.findViewById(R.id.price);
        }
    }
}