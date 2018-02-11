package com.dipitize.app.dipitize.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.adapter.MessagesRecyclerAdapter;
import com.dipitize.app.dipitize.model.Message;
import com.dipitize.app.dipitize.model.MessageThread;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InboxActivity extends AppCompatActivity {

    DatabaseReference database;

    MessagesRecyclerAdapter recyclerAdapter;
    RecyclerView recyclerView;

    ImageButton btnSend;
    EditText editMessage;

    Map<String, Object> messageThreadValues;

    MessageThread messageThread;

    User user;

    boolean isEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        database = FirebaseDatabase.getInstance().getReference();
        database.child("messageThreads").child(getIntent().getStringExtra("userId")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(MessageThread.class) == null) {
                    database.child("users").child(getIntent().getStringExtra("userId")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            isEnabled = true;
                            user = dataSnapshot.getValue(User.class);
                            List<Message> messages = new ArrayList<>();
                            messageThread = new MessageThread(user, messages);
                            messageThreadValues = messageThread.toMap();

                            recyclerAdapter = new MessagesRecyclerAdapter(getIntent().getStringExtra("userId"), messageThread.messages);
                            recyclerView.setAdapter(recyclerAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Snackbar.make(recyclerView, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                } else {
                    database.child("users").child(getIntent().getStringExtra("userId")).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            isEnabled = true;
                            user = dataSnapshot1.getValue(User.class);

                            messageThread = dataSnapshot.getValue(MessageThread.class);
                            messageThreadValues = messageThread.toMap();

                            List<Message> reverseMessages = Lists.reverse(messageThread.messages);

                            recyclerAdapter = new MessagesRecyclerAdapter(getIntent().getStringExtra("userId"), reverseMessages);
                            recyclerView.setAdapter(recyclerAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Snackbar.make(recyclerView, databaseError.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
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

                    Message message;
                    if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(getIntent().getStringExtra("userId"))) {
                        message = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), user, msg);
                        Notification notification = new Notification("JWsOb4iV7rYti9cSSqJgPjygxEK2", user.username + ": " + msg);
                        FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);
                    }
                    else {
                        message = new Message(FirebaseAuth.getInstance().getCurrentUser().getUid(), null, msg);
                        Notification notification = new Notification(getIntent().getStringExtra("userId"), "Admin: " + msg);
                        FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);

                        MyApplication.getInstance().sendFCM(user.fcmId, "Admin: " + msg);
                    }

                    List<Message> messages = messageThread.messages;
                    messages.add(message);
                    messageThreadValues.put("messages", messages);
                    database.child("messageThreads").child(getIntent().getStringExtra("userId")).updateChildren(messageThreadValues);
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
            Intent home_intent = new Intent(this, AccountActivity.class);
            home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(home_intent);
            finish();
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }
}
