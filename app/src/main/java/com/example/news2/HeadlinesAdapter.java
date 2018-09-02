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
import org.json.JSONObject;

public class HeadlinesAdapter extends RecyclerView.Adapter<HeadlinesAdapter.ViewHolder> {
    private JSONArray headlines = new JSONArray();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View headlineView;
        public ViewHolder(View view){
            super(view);
            headlineView = view;
        }
    }

    public void insertItem(JSONObject item){
       headlines.put(item);
       notifyItemInserted(getItemCount() - 1);
    }

    public void resetItems(){
        headlines = new JSONArray();
        notifyDataSetChanged();
    }

    public String getLink(View view){
        TextView tv = view.findViewById(R.id.textView6);
        return null;
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
        TextView urlTV = holder.headlineView.findViewById(R.id.textView4);
        String headlineTitle = "(no title)";
        String headlineQuote = "(no description)";
        String headlineUrl = null;
        try {
            headlineTitle = headlines.getJSONObject(position).getString("title");
            headlineQuote = headlines.getJSONObject(position).getString("description");
            headlineUrl = headlines.getJSONObject(position).getString("url");
        } catch (JSONException e){
            Log.d("onBindViewHolder", e.getMessage());
        }
        title.setText(headlineTitle);
        quote.setText(headlineQuote);
        urlTV.setText(headlineUrl);
    }

    // also invoked by the layout manager
    @Override
    public int getItemCount(){
        return headlines.length();
    }
}
