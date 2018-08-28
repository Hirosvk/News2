package com.example.news2;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/*
Projects:
    x insert debugger
    x RecyclerView changing items
    - Screen rotation and it's implication
    - open news link in a browser / webview
    - show images
    - deeper understanding of 'Context'

 */

public class MainActivity extends AppCompatActivity {
    private HeadlinesListFragment headlineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager manager = getSupportFragmentManager();
        headlineFragment = (HeadlinesListFragment) manager.findFragmentById(R.id.fragment);
    }

    public void loadHeadlines(View view){
        headlineFragment.loadHeadlines();
    }

    public void goAbout(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
