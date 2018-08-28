package com.example.news2;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

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

    public HeadlinesAdapter(JSONArray _headlines){
        headlines = new Bundle[_headlines.length()];
        for (int i = 0; i < _headlines.length(); i++){
            String title = "(no title)";
            String quote = "(no description)";
            try {
                title = _headlines.getJSONObject(i).getString("title");
                quote = _headlines.getJSONObject(i).getString("description");
            } catch (JSONException e){
                Log.d("HK:HeadlinesAdapter", e.getMessage());
            }
            headlines[i] = new Bundle();
            headlines[i].putString("title", title);
            headlines[i].putString("quote", quote);
        }
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
