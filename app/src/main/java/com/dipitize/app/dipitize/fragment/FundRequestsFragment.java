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
import com.dipitize.app.dipitize.adapter.FundRequestRecyclerAdapter;
import com.dipitize.app.dipitize.model.FundRequest;
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
public class FundRequestsFragment extends Fragment {

    DatabaseReference database;
    DatabaseReference requests;

    FundRequestRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;


    View fragmentView;

    public FundRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_fund_requests, container, false);

        database = FirebaseDatabase.getInstance().getReference();
        requests = database.child("fundRequests");

        final ProgressBar progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_search);

        requests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<FundRequest> requestList = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    requestList.add(child.getValue(FundRequest.class));
                    ids.add(child.getKey());
                }

                progressBar.setVisibility(View.GONE);

                recyclerAdapter = new FundRequestRecyclerAdapter(getContext(), requestList, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
                Snackbar.make(fragmentView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_fund_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

}
