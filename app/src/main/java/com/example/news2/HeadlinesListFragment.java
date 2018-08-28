package com.example.news2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public HeadlinesListFragment() {
        // Required empty public constructor
    }

    public void updateHeadlines(JSONArray articles){
        if (mAdapter!= null && mAdapter.getItemCount() > 0){
            for(int i = 0; i < articles.length(); i++){
                try {
                    mAdapter.insertItem(articles.getJSONObject(i));
                } catch (JSONException e){
                    Log.d("updateHeadlines", e.getMessage());
                }
            }
        } else {
            initializeRecyclerView(articles);
        }
    }

    private void initializeRecyclerView(JSONArray articles){
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.headlines_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // LayoutManager manages the scrolling direction, etc...
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HeadlinesAdapter(articles);
        mRecyclerView.setAdapter(mAdapter);

        final HeadlinesListFragment parentFragment = this;

        // TODO:
        // I don't understand this syntax. Do more research....
        //   This closure seems to be working, but are there any danger in this?
        //   What is this syntax, when instantiating OnScrollListener while overriding function.
        //   Lambda keeps coming up with closure. Study lambda.
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private HeadlinesListFragment fragment = parentFragment;
            @Override
            public void onScrolled(RecyclerView view, int dx, int dy ){
                Log.d("onScrolled", "onScrolled kicked in");
                super.onScrolled(view, dx, dy);
                if (!view.canScrollVertically(1)){
                    fragment.loadHeadlines();
                }
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        loadHeadlines();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        networkFragment = NetworkFragment.getInstance(getChildFragmentManager(), newsUrl);
        return inflater.inflate(R.layout.fragment_headlines_list, container, false);
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
        Log.d("HK:HLF.onProgressUpdate", "status: " + progressCode);
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
        // updates headlines
        JSONArray articles = null;
        try {
            articles = result.getJSONArray("articles");
        } catch (JSONException e){
            Log.d("HK:updateFromDownloads", e.getMessage());
        }

        if (articles != null) {
            updateHeadlines(articles);
        } else {
            Log.d("HK:updateFromDownloads", "articles is null");
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
            Log.d("HK:loadHeadlines", "Starting download");
            networkFragment.startDownload(newsUrl());
            downloading = true;
        }
    }

    public void resetHeadlines(){
        mAdapter.resetItems();
        pageNum = 1;
        loadHeadlines();
    }


    private String newsUrl(){
        String _url = newsUrl + "&page=" + pageNum.toString();
        pageNum++;
        return _url;
    }

}
