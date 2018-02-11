package com.dipitize.app.dipitize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.adapter.MatchChallengesRecyclerAdapter;
import com.dipitize.app.dipitize.model.Challenge;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MatchChallengeActivity extends AppCompatActivity {

    DatabaseReference database;
    DatabaseReference challenges;

    MatchChallengesRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_challenge);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (MyApplication.getInstance().selectedChallenge1 == null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }


        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_search);

        database = FirebaseDatabase.getInstance().getReference();
        challenges = database.child("challenges");

        challenges.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Challenge> challengesList = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Challenge challenge = child.getValue(Challenge.class);
                    if (challenge.category.equals(MyApplication.getInstance().selectedChallenge1.category) && challenge.amount == MyApplication.getInstance().selectedChallenge1.amount && !challenge.challenger.username.equals(MyApplication.getInstance().selectedChallenge1.challenger.username)) {
                        challengesList.add(challenge);
                        ids.add(child.getKey());
                    }
                }

                progressBar.setVisibility(View.GONE);

                recyclerAdapter = new MatchChallengesRecyclerAdapter(MatchChallengeActivity.this, challengesList, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_match_challenges);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent home_intent = new Intent(this, HomeActivity.class);
            home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(home_intent);
            finish();
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }
}
