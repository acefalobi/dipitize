package com.dipitize.app.dipitize.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.ChallengesRecyclerAdapter;
import com.dipitize.app.dipitize.adapter.GamesRecyclerAdapter;
import com.dipitize.app.dipitize.adapter.ResultsRecyclerAdapter;
import com.dipitize.app.dipitize.model.Challenge;
import com.dipitize.app.dipitize.model.Game;
import com.google.common.collect.Lists;
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
public class ResultFragment extends Fragment {

    DatabaseReference database;
    DatabaseReference games;

    ResultsRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    View fragmentView;

    public ResultFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_result, container, false);
        database = FirebaseDatabase.getInstance().getReference();
        games = database.child("games");

        final ProgressBar progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_search);

        games.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Game> gamesList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getValue(Game.class).isFinished) {
                        gamesList.add(child.getValue(Game.class));
                    }
                }
                gamesList = Lists.reverse(gamesList);

                progressBar.setVisibility(View.GONE);
                recyclerAdapter = new ResultsRecyclerAdapter(getContext(), gamesList);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(fragmentView, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }
}
