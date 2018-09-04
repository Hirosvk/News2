package com.example.news2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

public class HeadlinesListFragment extends Fragment implements DownloadCallback<JSONObject>{
    private RecyclerView mRecyclerView;
    private HeadlinesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private NetworkFragment networkFragment;
    private boolean downloading;

    private String newsUrl = "https://newsapi.org/v2/top-headlines?" +
            "country=us" +
            "&category=business" +
            "&pageSize=5" +
            "&apiKey=4291bc8cb2e847caa9155968b7140448";
    private Integer pageNum = 1;

    private JSONArray headlineArticles = new JSONArray();
    private JSONArray previousHeadlines;
    private AtomicInteger atomicInteger = new AtomicInteger();

    /* See Android documentation on custom view component */
    public static class HeadlinesRecyclerView extends RecyclerView {
        /*
            I'm not sure why I need to define these constructors here if the class
            inherits from RecyclerView, but runtime errors are thrown without these
            constructors.
        */
        public HeadlinesRecyclerView(Context context){
            super(context);
        }
        public HeadlinesRecyclerView(Context context, AttributeSet set){
            super(context, set);
        }
        public HeadlinesRecyclerView(Context context, AttributeSet set, int style){
            super(context, set, style);
        }
        @Override
        public void onChildAttachedToWindow(View child){
            super.onChildAttachedToWindow(child);
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv = v.findViewById(R.id.textView4);
                    HeadlinesListFragment.goToLink(getContext(), (String) tv.getText());
                }
            });
        }
    }

    public static void goToLink(Context context,  String link){
        Intent intent = new Intent(context, ArticleWebViewActivity.class);
        intent.putExtra("url", link);
        context.startActivity(intent);
    }

    public void updateHeadlines(JSONArray articles){
        if (mAdapter!= null){
            for(int i = 0; i < articles.length(); i++){
                try {
                    JSONObject newItem = articles.getJSONObject(i);
                    newItem.put("id", (long) atomicInteger.getAndIncrement());

                    headlineArticles.put(newItem);
                    int position = mAdapter.insertItem(newItem);
                    newItem.put("position", position);
                    getImage(newItem);

                } catch (JSONException e){
                    Log.d("updateHeadlines", e.getMessage());
                }
            }
        }
    }

    private void getImage(JSONObject article){
        String imageUrl = null;
        JSONObject extra = new JSONObject();
        try {
            imageUrl = article.getString("urlToImage");
            extra.put("position", article.getInt("position"));
            Log.d("DLX", "getting image " + article.getInt("position") + " " + imageUrl);
        } catch (JSONException e) {
            Log.d("getImage", e.getMessage());
        }

        if (imageUrl != null) {
            networkFragment.startDownload(imageUrl, NetworkFragment.DownloadType.IMAGE, extra);
        } else {
            Log.d("DLX", "No url for this article");
        }
    }

    private void initializeRecyclerView(){
        mRecyclerView = (HeadlinesRecyclerView) getView().findViewById(R.id.headlines_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // LayoutManager manages the scrolling direction, etc...
        Context context = getContext();
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HeadlinesAdapter();
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);

        final HeadlinesListFragment parentFragment = this;

        // Assign an instance of an anonymous class (this inherits from OnScrollListener)
        // Anonymous class is often used to add 'extra' ie. overriding to existing class/interface.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy ){
            super.onScrolled(view, dx, dy);
            if (!view.canScrollVertically(1)){
                parentFragment.loadHeadlines();
            /* Note on closure:
              - This underline font is 'implicit anonymous class parameter'.

              - Notice that parentFragment is declared 'final' in the enclosing function.

              - "When you create an instance of an anonymous inner class, any variables
                which are used within that class have their values copied in via the
                autogenerated constructor." (from StackFlow)

              - Compare this to JavaScript where inner functions retain reference to the environment.

              - "In Java, anonymous inner class provides closure-like functionality with some
                restrictions. For example, only final local variables can be used in their scope
                – better said, their values can be read.

                JavaScript allows full access to the outer scope variables and functions. They
                can be read, written, and if needed even hidden by local definitions." (from blog)
             */
            }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        String articles = headlineArticles.toString();
        outState.putString("articles", articles);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        updateHeadlineArticlesWithPrevious(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        networkFragment = NetworkFragment.getInstance(getChildFragmentManager());
        return inflater.inflate(R.layout.fragment_headlines_list, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();
        initializeRecyclerView();
        if (previousHeadlines != null && previousHeadlines.length() > 0){
            updateHeadlines(previousHeadlines);
            previousHeadlines = null;
        } else {
            loadHeadlines();
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void updateHeadlineArticlesWithPrevious(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            String oldArticlesString = savedInstanceState.getString("articles");
            JSONArray oldArticles = null;
            try {
                oldArticles = new JSONArray(oldArticlesString);
            } catch (JSONException e) {
                Log.d("HLF.onCreate", e.getMessage());
            }
            if (oldArticles != null && oldArticles.length() > 0) {
                previousHeadlines = oldArticles;
            }
        }
    }

    /* Implementation functions */
    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager manager = (ConnectivityManager) getActivity()
                                      .getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode) {
        switch(progressCode){
            case Progress.CONNECT_SUCCESS:
                // do something in UI.
                break;
            case Progress.ERROR:
                // do something in UI.
                break;
        }
    }

    @Override
    public void updateFromDownloads(JSONObject result) {
        if (result.has("downloadedImage")) {
            try {
                Log.d("DLX", "received image");
                int position = result.getInt("position");
                Bitmap image = (Bitmap) result.get("downloadedImage");
                mAdapter.updateImage(position, image);

            } catch (JSONException e){
                Log.d("updateFromDownloads", e.getMessage());
            }


        } else if (result.has("articles")){
            JSONArray articles = null;
            try {
                articles = result.getJSONArray("articles");
            } catch (JSONException e) {
                Log.d("HK:updateFromDownloads", e.getMessage());
            }

            if (articles != null) {
                updateHeadlines(articles);
            } else {
                Log.d("HK:updateFromDownloads", "articles is null");
            }

        } else {
            Log.d("updateFromDownloads", "Wrong download response");
        }
    }

    @Override
    public void finishedDownloading() {
        downloading = false;
        if (networkFragment != null){
            networkFragment.cancelDownload();
        }
    }
    /* Implementation Functions */

    public void loadHeadlines(){
        if (!downloading && networkFragment != null) {
            networkFragment.startDownload(newsUrl());
            downloading = true;
        }
    }

    public void resetHeadlines(){
        headlineArticles = new JSONArray();
        if (mAdapter != null) {
            mAdapter.resetItems();
        }
        if (pageNum > 1) {
            pageNum = 1;
        }
        loadHeadlines();
    }


    private String newsUrl(){
        String _url = newsUrl + "&page=" + pageNum.toString();
        pageNum++;
        return _url;
    }

}
