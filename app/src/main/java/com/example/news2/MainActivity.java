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

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void loadHedadlines(View view){
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_headlines);
        mRecyclerView.setHasFixedSize(true);

        // LayoutManager manages the scrolling direction, etc...
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new HeadlinesAdapter(bundledHeadlines());
        mRecyclerView.setAdapter(mAdapter);
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
