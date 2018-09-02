package com.example.news2;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/*
Projects:
    x insert debugger
    x RecyclerView changing items
    x Screen rotation and it's implication
    x open news link in a browser / webview
        -> onClick handled in fragment
        -> open webview
    - show images
    - write tests

    - Screen Rotation: change layout by orientation
    - Screen Rotation: Save headlines & network connection after screen rotation
      using onSaveInstanceState is ok for now.
      Consider using ViewModel and Service to download stuff for the future.
        -> can Fragment's ViewModel live through config change?
      Using Parcelable is another idea, but only for small data
 */

public class MainActivity extends AppCompatActivity {
    public HeadlinesListFragment headlineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("track", "MainActivity.onCreate");
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        headlineFragment = (HeadlinesListFragment) manager.findFragmentById(R.id.fragment);
    }

    @Override
    protected void onDestroy(){
        Log.d("track", "MainActivity.onDestroy");
       super.onDestroy();
    }

    public void resetHeadlines(View view){
        headlineFragment.resetHeadlines();
    }
    public void goAbout(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
