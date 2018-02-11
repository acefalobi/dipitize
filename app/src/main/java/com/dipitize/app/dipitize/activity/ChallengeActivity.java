package com.dipitize.app.dipitize.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.Challenge;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ChallengeActivity extends AppCompatActivity {

    Challenge challenge;

    ImageView imageView;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;

    DatabaseReference database;

    FirebaseStorage firebaseStorage;
    StorageReference storage;
    StorageReference mediaReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        progressBar = (ProgressBar) findViewById(R.id.progress_challenge);

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        storage = firebaseStorage.getReferenceFromUrl("gs://dipitize.appspot.com");

        database = FirebaseDatabase.getInstance().getReference();

        database.child("challenges").child(getIntent().getStringExtra("challengeId")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                challenge = dataSnapshot.getValue(Challenge.class);

                mediaReference = storage.child("Images").child(challenge.mediaUrl);

                imageView = (ImageView) findViewById(R.id.image_challenge);
                mediaReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (getApplicationContext() != null && !isFinishing()) {
                            Glide.with(ChallengeActivity.this).load(uri.toString())
                                    .thumbnail(0.5f)
                                    .override(200, 200)
                                    .crossFade()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(new GlideDrawableImageViewTarget(imageView) {
                                        @Override
                                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                            super.onResourceReady(resource, animation);
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Snackbar.make(imageView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent home_intent = new Intent(this, AdminActivity.class);
            home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(home_intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
