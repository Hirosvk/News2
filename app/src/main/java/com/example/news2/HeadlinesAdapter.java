package com.example.news2;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HeadlinesAdapter extends RecyclerView.Adapter<HeadlinesAdapter.ViewHolder> {
    private Bundle[] headlines;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View headlineView;
        public ViewHolder(View view){
            super(view);
            headlineView = view;
        }
    }

    public HeadlinesAdapter(Bundle[] _headlines){
        headlines = _headlines;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HeadlinesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
       View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.headline_text_view, parent, false);
       return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        TextView title = holder.headlineView.findViewById(R.id.textView6);
        TextView quote = holder.headlineView.findViewById(R.id.textView7);
        String headlineTitle = headlines[position].getString("title");
        String headlineQuote = headlines[position].getString("quote");
        title.setText(headlineTitle);
        quote.setText(headlineQuote);
    }

    // also invoked by the layout manager
    @Override
    public int getItemCount(){
        return headlines.length;
    }
}
