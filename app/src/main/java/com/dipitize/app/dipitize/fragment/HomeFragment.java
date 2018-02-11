package com.dipitize.app.dipitize.fragment;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.adapter.GamesRecyclerAdapter;
import com.dipitize.app.dipitize.model.Game;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    DatabaseReference database;
    DatabaseReference games;

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

        final ProgressBar progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_search);

        games.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                List<Game> gamesList = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final Game game = child.getValue(Game.class);
                    if (!game.isFinished) {
                        long age = System.currentTimeMillis() - game.timeStarted;
                        if (age < 86400000) {
                            gamesList.add(child.getValue(Game.class));
                            ids.add(child.getKey());
                        } else {
                            List<String> voters1 = new ArrayList<>();
                            List<String> voters2 = new ArrayList<>();
                            if (game.voters1 != null) {
                                voters1 = game.voters1;
                            }
                            if (game.voters2 != null) {
                                voters2 = game.voters2;
                            }
                            if (voters1.size() == voters2.size()) {
                                gamesList.add(child.getValue(Game.class));
                                ids.add(child.getKey());
                            } else {
                                final int winner;

                                if (voters1.size() > voters2.size()) {
                                    winner = 0;
                                } else {
                                    winner = 1;
                                }
                                final Map<String, Object> gameValues = game.toMap();
                                gameValues.put("isFinished", true);
                                gameValues.put("winner", game.playerIds.get(winner));
                                final List<String> finalVoters2 = voters2;
                                final List<String> finalVoters1 = voters1;
                                child.getRef().updateChildren(gameValues, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                                            database.child("users").child(game.playerIds.get(winner)).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    database.child("users").child(game.playerIds.get(winner)).child("balance")
                                                            .setValue(dataSnapshot.getValue(User.class).balance + (game.amount * 2));
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            for (String judge : finalVoters1) {
                                                Notification notification = new Notification(judge, "A game you voted for has ended and @" + game.players.get(winner).username + " won");
                                                database.child("notifications").push().setValue(notification);
                                            }
                                            for (String judge : finalVoters2) {
                                                Notification notification = new Notification(judge, "A game you voted for has ended and @" + game.players.get(winner).username + " won");
                                                database.child("notifications").push().setValue(notification);
                                            }
                                            if (winner == 0) {
                                                Notification notification1 = new Notification(game.playerIds.get(0), "Your game with @" + game.players.get(1).username + " has ended. You won");
                                                Notification notification2 = new Notification(game.playerIds.get(1), "Your game with @" + game.players.get(0).username + " has ended. You Lost!");
                                                database.child("notifications").push().setValue(notification2);
                                                database.child("notifications").push().setValue(notification1);
                                                MyApplication.getInstance().sendFCM(game.players.get(0).fcmId, "Your game with @" + game.players.get(1).username + " has ended. You Won!");
                                                MyApplication.getInstance().sendFCM(game.players.get(1).fcmId, "Your game with @" + game.players.get(0).username + " has ended. You Lost!");
                                            } else {
                                                Notification notification1 = new Notification(game.playerIds.get(1), "Your game with @" + game.players.get(0).username + " has ended. You won");
                                                Notification notification2 = new Notification(game.playerIds.get(0), "Your game with @" + game.players.get(1).username + " has ended. You Lost!");
                                                database.child("notifications").push().setValue(notification2);
                                                database.child("notifications").push().setValue(notification1);
                                                MyApplication.getInstance().sendFCM(game.players.get(1).fcmId, "Your game with @" + game.players.get(0).username + " has ended. You Won!");
                                                MyApplication.getInstance().sendFCM(game.players.get(0).fcmId, "Your game with @" + game.players.get(1).username + " has ended. You Lost!");
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
                gamesList = Lists.reverse(gamesList);
                ids = Lists.reverse(ids);
                progressBar.setVisibility(View.GONE);

                recyclerAdapter = new GamesRecyclerAdapter(getContext(), gamesList, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(fragmentView, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_games);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

}
