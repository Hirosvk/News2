package com.example.news2;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HeadlinesListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class HeadlinesListFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;

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

    public HeadlinesListFragment() {
        // Required empty public constructor
    }

    public void loadHedadlines(){
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.headlines_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // LayoutManager manages the scrolling direction, etc...
        mLayoutManager = new LinearLayoutManager(getContext());
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_headlines_list, container, false);
    }
/*
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
/*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
*/
}
