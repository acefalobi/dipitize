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

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.WithdrawRequestRecyclerAdapter;
import com.dipitize.app.dipitize.model.WithdrawRequest;
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
public class WithdrawRequestsFragment extends Fragment {

    DatabaseReference database;
    DatabaseReference requests;

    WithdrawRequestRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;


    View fragmentView;


    public WithdrawRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_withdraw_requests, container, false);

        database = FirebaseDatabase.getInstance().getReference();
        requests = database.child("withdrawRequests");

        final ProgressBar progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_search);

        requests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<WithdrawRequest> requestList = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    requestList.add(child.getValue(WithdrawRequest.class));
                    ids.add(child.getKey());
                }

                progressBar.setVisibility(View.GONE);

                recyclerAdapter = new WithdrawRequestRecyclerAdapter(getContext(), requestList, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
                Snackbar.make(fragmentView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_withdraw_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

}
