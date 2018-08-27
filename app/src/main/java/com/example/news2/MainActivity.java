package com.example.news2;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;


/*
Projects:
    - make http request to get news api
        https://developer.android.com/training/basics/network-ops/connecting
        The above link uses fragment. Are there other ways?
        Research other async process.
    - insert debugger

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
        headlines.loadHeadlines();
    }
/*
    // I first thought that a list of headlines should be implemented as a list
    // of fragments. Now I'm questioning that; fragments would host a RecyclerView
    // which would contain list of items.
    public void loadSingleHeadline(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        HeadlineFragment fragment = new HeadlineFragment();
        fragmentTransaction.add(R.id.fragmentLayout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
*/

    public void goAbout(View view){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
}
