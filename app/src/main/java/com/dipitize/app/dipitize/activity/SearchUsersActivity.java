package com.dipitize.app.dipitize.activity;

import android.app.SearchManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.SearchUsersRecyclerAdapter;
import com.dipitize.app.dipitize.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersActivity extends AppCompatActivity {

    DatabaseReference database;
    DatabaseReference usersReference;

    SearchUsersRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String queryString = getIntent().getStringExtra(SearchManager.QUERY);

        getSupportActionBar().setTitle("Search: " + queryString);

        progressBar = (ProgressBar) findViewById(R.id.progress_search);

        database = FirebaseDatabase.getInstance().getReference();

        usersReference = database.child("users");
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user.username.startsWith(queryString) || user.fullName.startsWith(queryString)) {
                        users.add(user);
                        ids.add(child.getKey());
                    }
                }
                progressBar.setVisibility(View.GONE);
                recyclerAdapter = new SearchUsersRecyclerAdapter(SearchUsersActivity.this, users, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Snackbar.make(recyclerView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_search_users);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
    }
}
