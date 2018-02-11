package com.dipitize.app.dipitize;

import android.content.Intent;
import android.widget.Toast;

import com.dipitize.app.dipitize.activity.HomeActivity;
import com.dipitize.app.dipitize.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Map;

/**
 * Made by acefalobi on 3/16/2017.
 */
public class MyFirebaseInstantIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(final String refreshedToken) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            if (!firebaseUser.getEmail().equals("admin@dipitize.com")) {
                FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        Map<String, Object> map = user.toMap();
                        map.put("fcmId", refreshedToken);
                        FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).updateChildren(map);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
