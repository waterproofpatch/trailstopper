package com.example.trailstopper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.Viewholder> {

    private ArrayList<Stock> stockArrayList;
    private Context context;

    // Constructor
    public StockAdapter(Context context, ArrayList<Stock> stockArrayList) {
        this.stockArrayList = stockArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public StockAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StockAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        Stock stock = stockArrayList.get(position);
        if (stock == null) {
            return;
        }
        holder.ticker.setText(stock.getTicker());
        holder.name.setText(stock.getLongName());
        holder.price.setText("Current: " + stock.getPrice());
        holder.atr.setText("ATR: " + stock.getAtr());
        holder.trailStop.setText("Trail Stop: " + stock.getTrailStop() + " (" + stock.getTrailStopPct() +"%)");

        holder.buttonRemoveStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Stock s = stockArrayList.remove(holder.getAdapterPosition());
                s.stopUpdates();
                notifyDataSetChanged();
            }
        });
        holder.buttonEditStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), StockActivity.class);
                Bundle b = new Bundle();
                b.putString("ticker", stock.getTicker());
                i.putExtras(b);
                view.getContext().startActivity(i);
            }
        });
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
        private TextView atr;
        private TextView name;
        private TextView trailStop;
        private Button buttonRemoveStock;
        private Button buttonEditStock;

        public Viewholder(@NonNull final View itemView) {
            super(itemView);
            ticker = itemView.findViewById(R.id.ticker);
            name = itemView.findViewById(R.id.name);
            price = itemView.findViewById(R.id.price);
            atr = itemView.findViewById(R.id.atr);
            trailStop = itemView.findViewById(R.id.trail_stop);
            buttonRemoveStock = (Button)itemView.findViewById(R.id.buttonRemoveStock);
            buttonEditStock = (Button)itemView.findViewById(R.id.buttonEditStock);
        }
    }
}
