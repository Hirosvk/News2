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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class HeadlinesListFragment extends Fragment implements DownloadCallback<JSONObject>{
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private NetworkFragment networkFragment;
    private boolean downloading;

    private String newsUrl = "https://newsapi.org/v2/top-headlines?" +
            "country=us" +
            "&category=business" +
            "&pageSize=5" +
            "&apiKey=4291bc8cb2e847caa9155968b7140448";

    public HeadlinesListFragment() {
        // Required empty public constructor
    }

    public void loadHeadlines(){
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.headlines_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // LayoutManager manages the scrolling direction, etc...
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HeadlinesAdapter(bundledHeadlines());
        mRecyclerView.setAdapter(mAdapter);

        Log.d("loadHeadlines", "Starting download");
        networkFragment.startDownload();
    }

    public Bundle[] bundledHeadlines(){
        int size = headlineTitles.length;
        Bundle[] bundledHeadlines = new Bundle[size];
        for(int i = 0; i < size; i++){
            Bundle b = new Bundle();
            b.putString("title", headlineTitles[i]);
            b.putString("quote", headlineQuotes[i]);
            bundledHeadlines[i] = b;
        }
        return bundledHeadlines;
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
        // Should permission be checked before using the manager?
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
        // updates headlines

        Iterator<String> keys = result.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Log.d("updateFromDownloads", key + result.getString(key));
            }
        } catch (JSONException e){
            Log.d("updateFromDownloads", e.getMessage());
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

    private String[] headlineTitles = new String[]{
            "Monica Hall",
            "Gavin Belson",
            "Jack \"Action Jack\" Barker",
            "Nelson \"Big Head\" Bighetti",
            "Donald \"Jared\" Dunn",
            "Jian Yang",
            "Ron LaFlamme",
            "Peter Gregory"
    };

    private String[] headlineQuotes = new String[]{
            "I firmly believe we can only achieve greatness if first, we achieve goodness",
            "I was gonna sleep last night, but, uh... I thought I had this solve for this computational trust issue I've been working on, but it turns out, I didn't have a solve. But it was too late. I had already drank the whole pot of coffee.",
            "You listen to me, you muscle-bound handsome Adonis: tech is reserved for people like me, okay? The freaks, the weirdos, the misfits, the geeks, the dweebs, the dorks! Not you!",
            "And that, gentlemen, is scrum. Welcome to the next eight weeks of our lives.",
            "Gentlemen, I just paid the palapa contractor. The palapa piper, so to speak. The dream is a reality. We'll no longer be exposed... to the elements.",
            "Let me ask you. How fast do you think you could jerk off every guy in this room? Because I know how long it would take me. And I can prove it",
            "I simply imagine that my skeleton is me and my body is my house. That way I'm always home.",
            "Gavin Belson started out with lofty goals too, but he just kept excusing immoral behavior just like this, until one day all that was left was a sad man with funny shoes... Disgraced, friendless, and engorged with the blood of a youthful charlatan."
    };

}
