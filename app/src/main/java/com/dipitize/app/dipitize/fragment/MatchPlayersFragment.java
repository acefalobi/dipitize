package com.dipitize.app.dipitize.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.ChallengesRecyclerAdapter;
import com.dipitize.app.dipitize.model.Challenge;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MatchPlayersFragment extends Fragment {

    DatabaseReference database;
    DatabaseReference challenges;

    ChallengesRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    View fragmentView;

    public MatchPlayersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_match_players, container, false);

        database = FirebaseDatabase.getInstance().getReference();
        challenges = database.child("challenges");

        final ProgressBar progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_search);

        challenges.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Challenge> challengesList = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    challengesList.add(child.getValue(Challenge.class));
                    ids.add(child.getKey());
                }

                progressBar.setVisibility(View.GONE);

                recyclerAdapter = new ChallengesRecyclerAdapter(getContext(), challengesList, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
                Snackbar.make(fragmentView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_match_players);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
        return fragmentView;
    }

}
