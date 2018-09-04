package com.example.news2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public int insertItem(JSONObject item){
        headlines.put(item);
        int position = getItemCount();
        notifyItemInserted(position - 1);
        return position - 1;
        // returns the position of the item in headlines.
        // "position" seems to refer to items' position in headlines,
        // not the position of ViewHolder
    }

    public void updateImage(int position, Bitmap image){
        try {
            JSONObject item = headlines.getJSONObject(position);
            item.put("downloadedImage", image);
            notifyItemChanged(position); // This triggers onBindViewHolder

        } catch (JSONException e){
            Log.d("updateImage", e.getMessage());
        }
    }

    // I overrode this function to identify to which item image should be inserted.
    // However, since position suffices for this purpose, this overriding is unnecessary.
    // Leaving it here for reference.
    @Override
    public long getItemId(int position){
        JSONObject item;
        long itemId = View.NO_ID;

        try {
            item = headlines.getJSONObject(position);
            long _itemId = item.getInt("id");
            if (!((Long)_itemId).equals(null)){
                itemId = _itemId;
            }
        } catch (JSONException e) {
            Log.d("getItemId", e.getMessage());
        }

        return itemId;
    }

    public void resetItems(){
        headlines = new JSONArray();
        notifyDataSetChanged();
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
        try {
            TextView title = holder.headlineView.findViewById(R.id.textView6);
            TextView quote = holder.headlineView.findViewById(R.id.textView7);
            TextView urlTV = holder.headlineView.findViewById(R.id.textView4);

            String headlineTitle = "(no title)";
            String headlineQuote = "(no description)";
            String headlineUrl = null;

            Bitmap image;
            JSONObject item;

            item = headlines.getJSONObject(position);
            headlineTitle = item.getString("title");
            headlineQuote = item.getString("description");
            headlineUrl = item.getString("url");

            title.setText(headlineTitle);
            quote.setText(headlineQuote);
            urlTV.setText(headlineUrl);

            if (item != null && item.has("downloadedImage")) {
                Log.d("DLX", "image is here!");
                image = (Bitmap) item.get("downloadedImage");
                ImageView iv = holder.headlineView.findViewById(R.id.imageView);
                iv.setImageBitmap(image);
            }

        } catch (JSONException e){
            Log.d("onBindViewHolder", e.getMessage());
        }
    }

    // also invoked by the layout manager
    @Override
    public int getItemCount(){
        return headlines.length();
    }
}
