package com.dipitize.app.dipitize.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.MessageThreadRecyclerAdapter;
import com.dipitize.app.dipitize.model.DMThread;
import com.google.firebase.auth.FirebaseAuth;
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
public class MessagesFragment extends Fragment {

    DatabaseReference databaseReference;

    MessageThreadRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    View fragmentView;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_messages, container, false);

        final ProgressBar progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_search);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("dmThreads").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<DMThread> messageThreads = new ArrayList<>();
                List<String> ids = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    DMThread thread = child.getValue(DMThread.class);
                    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                    if (firebaseAuth.getCurrentUser() != null) {
                        if (thread.userId1.equals(firebaseAuth.getCurrentUser().getUid())
                                || thread.userId2.equals(firebaseAuth.getCurrentUser().getUid())) {
                            messageThreads.add(thread);
                            ids.add(child.getKey());
                        }
                    }
                }

                progressBar.setVisibility(View.GONE);

                recyclerAdapter = new MessageThreadRecyclerAdapter(getActivity(), messageThreads, ids);
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.recycler_messages);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setHasFixedSize(true);

        return fragmentView;
    }

}
