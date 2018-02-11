package com.dipitize.app.dipitize.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.activity.CreateChallengeActivity;
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
public class ResultFragment extends Fragment implements View.OnClickListener {

    DatabaseReference database;
    DatabaseReference challenges;

    FloatingActionButton buttonGotoCreateChallenge;

    ChallengesRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    View fragmentView;

    public ResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_play, container, false);

        buttonGotoCreateChallenge = (FloatingActionButton) fragmentView.findViewById(R.id.button_goto_create_challenge);
        buttonGotoCreateChallenge.setOnClickListener(this);

        database = FirebaseDatabase.getInstance().getReference();
        challenges = database.child("challenges");

        challenges.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Challenge> challengesList = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    challengesList.add(child.getValue(Challenge.class));
                    ids.add(child.getKey());
                }

                recyclerAdapter = new ChallengesRecyclerAdapter(getContext(), challengesList, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(fragmentView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_challenges);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_goto_create_challenge) {
            startActivity(new Intent(getContext(), CreateChallengeActivity.class));
        }
    }
}
