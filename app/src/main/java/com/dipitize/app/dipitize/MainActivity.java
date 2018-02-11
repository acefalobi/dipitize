package com.dipitize.app.dipitize;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.activity.AdminActivity;
import com.dipitize.app.dipitize.activity.HomeActivity;
import com.dipitize.app.dipitize.activity.LoginActivity;
import com.dipitize.app.dipitize.activity.NotificationsActivity;
import com.dipitize.app.dipitize.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            if (firebaseUser.getEmail().equals("admin@dipitize.com")) {
                startActivity(new Intent(this, AdminActivity.class));
                finish();
            } else {
                FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        Map<String, Object> map = user.toMap();
                        map.put("fcmId", FirebaseInstanceId.getInstance().getToken());
                        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).updateChildren(map);
                        if (user.isBlocked) {
                            Toast.makeText(MainActivity.this, "Sorry, you've been blocked!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            if (getIntent().getBooleanExtra("isNotify", false)) {
                                startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        if (getIntent().getBooleanExtra("isNotify", false)) {
                            startActivity(new Intent(MainActivity.this, NotificationsActivity.class));
                            finish();
                        } else {
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();
                        }
                    }
                });
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}