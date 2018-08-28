package com.example.news2;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/*
Projects:
    - insert debugger
    - RecyclerView changing items/using DataObserver
    - Screen rotation and it's implication
    - open news link in a browser / webview
    - show images
    - deeper understanding of 'Context'

 */

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadHeadlines(View view){
        FragmentManager manager = getSupportFragmentManager();
        HeadlinesListFragment headlines = (HeadlinesListFragment) manager
                                                                  .findFragmentById(R.id.fragment);
        headlines.startDownload();
    }

    public void goAbout(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
