package com.dipitize.app.dipitize.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.GamesRecyclerAdapter;
import com.dipitize.app.dipitize.model.Game;
import com.dipitize.app.dipitize.model.GameMembers;
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
public class HomeFragment extends Fragment {

    DatabaseReference database;
    DatabaseReference games;
    DatabaseReference gameMembers;

    GamesRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    View fragmentView;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false);
        database = FirebaseDatabase.getInstance().getReference();
        games = database.child("games");
        gameMembers = database.child("gameMembers");

        games.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<Game> gamesList = new ArrayList<>();
                final List<String> ids = new ArrayList<>();
                boolean isValid = true;
                String invalidGameId = "";

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Game game = child.getValue(Game.class);
                    long length = 0;
                    if (game.type == 1) {
                        length = 1800000;
                    } else  if (game.type == 2) {
                        length = 18000000;
                    } else if (game.type == 3){
                        length = 36000000;
                    }  else if (game.type == 4){
                        length = 86400000;
                    }
                    long age = System.currentTimeMillis() - game.timeStarted;
                    long life = length - age;
                    if (life > 0) {
                        gamesList.add(child.getValue(Game.class));
                        ids.add(child.getKey());
                    } else {
                        child.getRef().removeValue();
                        isValid = false;
                        invalidGameId = child.getKey();
                    }
                }

                final boolean finalIsValid = isValid;
                final String finalInvalidGameId = invalidGameId;
                gameMembers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<GameMembers> gameMembersList = new ArrayList<>();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            gameMembersList.add(child.getValue(GameMembers.class));
                            if (!finalIsValid) {
                                if (finalInvalidGameId.equals(child.getKey())) {
                                    child.getRef().removeValue();
                                }
                            }
                        }

                        recyclerAdapter = new GamesRecyclerAdapter(getContext(), gamesList, gameMembersList, ids);
                        recyclerView.setAdapter(recyclerAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Snackbar.make(fragmentView, "Error retrieving game members", Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(fragmentView, "Error retrieving games", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_games);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

}
