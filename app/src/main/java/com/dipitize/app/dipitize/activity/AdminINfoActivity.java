package com.dipitize.app.dipitize.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.adapter.NotificationsRecyclerAdapter;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminINfoActivity extends AppCompatActivity {

    DatabaseReference databaseReference;
    DatabaseReference notifications;

    NotificationsRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_info);

        textView = (TextView) findViewById(R.id.text_total_balance);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long totalBalance = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    totalBalance += child.getValue(User.class).balance;
                }
                textView.setText(String.valueOf(totalBalance));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(textView, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
        notifications = databaseReference.child("notifications");

        notifications.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Notification> notificationsList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Notification notification = child.getValue(Notification.class);
                    if (notification.receiver.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        notificationsList.add(notification);
                        Map<String, Object> notificationValues = notification.toMap();
                        notificationValues.put("isRead", true);
                        notifications.child(child.getKey()).updateChildren(notificationValues);
                    }
                }

                recyclerAdapter = new NotificationsRecyclerAdapter(Lists.reverse(notificationsList));
                recyclerView.setAdapter(recyclerAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(recyclerView, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }
}
