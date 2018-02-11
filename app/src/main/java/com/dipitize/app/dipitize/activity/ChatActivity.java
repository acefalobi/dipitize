package com.dipitize.app.dipitize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.adapter.ChatRecyclerAdapter;
import com.dipitize.app.dipitize.model.DMThread;
import com.dipitize.app.dipitize.model.Message;
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

public class ChatActivity extends AppCompatActivity {

    DatabaseReference database;

    ChatRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    ImageButton btnSend;
    EditText editMessage;

    Map<String, Object> messageThreadValues;

    DMThread messageThread;

    User user;

    boolean isEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        database = FirebaseDatabase.getInstance().getReference();
        database.child("dmThreads").child(getIntent().getStringExtra("dmId")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot1) {
                        isEnabled = true;
                        user = dataSnapshot1.getValue(User.class);

                        messageThread = dataSnapshot.getValue(DMThread.class);
                        messageThreadValues = messageThread.toMap();

                        if (messageThread.userId1.equals(dataSnapshot1.getKey())) {
                            getSupportActionBar().setTitle("@" + messageThread.user2.username);
                        } else {
                            getSupportActionBar().setTitle("@" + messageThread.user1.username);
                        }

                        if (messageThread.messages == null) {
                            messageThread.messages = new ArrayList<>();
                        }

                        List<Message> reverseMessages = Lists.reverse(messageThread.messages);

                        recyclerAdapter = new ChatRecyclerAdapter(FirebaseAuth.getInstance().getCurrentUser().getUid(), reverseMessages);
                        recyclerView.setAdapter(recyclerAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Snackbar.make(recyclerView, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(recyclerView, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        editMessage = (EditText) findViewById(R.id.edit_message);
        btnSend = (ImageButton) findViewById(R.id.btn_send_message);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editMessage.getText().toString().trim().isEmpty() && isEnabled) {
                    String msg = editMessage.getText().toString();
                    editMessage.setText("");

                    Message message = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), user, msg);
                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageThread.userId1)) {
                        Notification notification = new Notification(messageThread.userId2, user.username + ": " + msg);
                        FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);
                        MyApplication.getInstance().sendFCM(messageThread.user2.fcmId, user.username + ": " + msg);
                    } else {
                        Notification notification = new Notification(messageThread.userId1, user.username + ": " + msg);
                        FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);
                        MyApplication.getInstance().sendFCM(messageThread.user1.fcmId, user.username + ": " + msg);
                    }

                    List<Message> messages = messageThread.messages;
                    messages.add(message);
                    messageThreadValues.put("messages", messages);
                    database.child("dmThreads").child(getIntent().getStringExtra("dmId")).updateChildren(messageThreadValues);
                }
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_messages);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);
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
