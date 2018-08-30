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
    - Screen rotation and it's implication
    - open news link in a browser / webview
    - show images
    - deeper understanding of 'Context'
    - write tests

    - using headless fragment to donwload headlines is ok for now.
      consider using ViewModel and Service to download stuff for the future.
 */

public class MainActivity extends AppCompatActivity {
    private HeadlinesListFragment headlineFragment;

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
