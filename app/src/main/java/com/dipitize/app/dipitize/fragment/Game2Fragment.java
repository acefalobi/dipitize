package com.dipitize.app.dipitize.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.activity.ChatActivity;
import com.dipitize.app.dipitize.model.DMThread;
import com.dipitize.app.dipitize.model.Game;
import com.dipitize.app.dipitize.model.Message;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class Game2Fragment extends Fragment {

    Game game;

    ImageView imageView;
    ProgressBar progressBar;

    TextView textVotes, textLife;

    FloatingActionButton floatingActionButton;
    AppCompatButton voteButton;
    ImageButton messageButton;

    FirebaseAuth firebaseAuth;

    DatabaseReference database;

    FirebaseStorage firebaseStorage;
    StorageReference storage;
    StorageReference mediaReference;

    Map<String, Object> gameValues;

    View fragmentView;

    public Game2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_game2, container, false);

        progressBar = (ProgressBar) fragmentView.findViewById(R.id.progress_game2);

        textVotes = (TextView) fragmentView.findViewById(R.id.text_votes2);
        textLife = (TextView) fragmentView.findViewById(R.id.text_life2);

        voteButton = (AppCompatButton) fragmentView.findViewById(R.id.button_vote_game2);
        messageButton = (ImageButton) fragmentView.findViewById(R.id.btn_goto_message2);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Loading...");
                progressDialog.setIndeterminate(true);
                progressDialog.setCancelable(false);
                progressDialog.show();
                database.child("users").child(firebaseAuth.getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final User currentUser = dataSnapshot.getValue(User.class);
                                database.child("dmThreads").addListenerForSingleValueEvent(new ValueEventListener() {;
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean hasBeenInitialized = false;
                                        String key = "";
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            DMThread thread = child.getValue(DMThread.class);
                                            if (firebaseAuth.getCurrentUser() != null) {
                                                if ((thread.userId1.equals(game.playerIds.get(1))
                                                        && thread.userId2.equals(firebaseAuth.getCurrentUser().getUid()))
                                                        || (thread.userId2.equals(game.playerIds.get(1))
                                                        && thread.userId1.equals(firebaseAuth.getCurrentUser().getUid()))) {
                                                    hasBeenInitialized = true;
                                                    key = child.getKey();
                                                }
                                            }
                                        }
                                        if (!hasBeenInitialized) {
                                            DMThread dmThread = new DMThread(game.players.get(1), currentUser,
                                                    game.playerIds.get(1), firebaseAuth.getCurrentUser().getUid(), new ArrayList<Message>());
                                            database.child("dmThreads").push().setValue(dmThread, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                                                        intent.putExtra("dmId", databaseReference.getKey());
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    }
                                                }
                                            });
                                        } else {
                                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                                            intent.putExtra("dmId", key);
                                            startActivity(intent);
                                            getActivity().finish();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        floatingActionButton = (FloatingActionButton) fragmentView.findViewById(R.id.button_goto_game1);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ViewPager) container.findViewById(R.id.view_pager_game)).setCurrentItem(0, true);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseStorage = FirebaseStorage.getInstance();
        storage = firebaseStorage.getReferenceFromUrl("gs://dipitize.appspot.com");

        database = FirebaseDatabase.getInstance().getReference();

        database.child("games").child(getActivity().getIntent().getStringExtra("gameId")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game = dataSnapshot.getValue(Game.class);
                gameValues = game.toMap();
                long age = System.currentTimeMillis() - game.timeStarted;
                age = 86400000 - age;
                if (age < 1000) {
                    textLife.setText("Next Vote Wins");
                } else if (age < 60000) {
                    textLife.setText(String.valueOf(age / 1000) + "s left");
                } else if (age < 3600000) {
                    textLife.setText(String.valueOf((age / 1000) / 60) + "m left");
                } else if (age < 86400000) {
                    textLife.setText(String.valueOf(((age / 1000) / 60) / 60) + "h left");
                } else {
                    textLife.setText(String.valueOf((((age / 1000) / 60) / 60) / 24) + "d left");
                }

                mediaReference = storage.child("Images").child(game.mediaUrls.get(1));

                final List<String> voters;
                if (game.voters2 != null) {
                    voters = game.voters2;
                } else {
                    voters = new ArrayList<>();
                }
                textVotes.setText(String.valueOf(voters.size()) + " votes");

                if (!firebaseAuth.getCurrentUser().getUid().equals(game.playerIds.get(1)) && !game.playerIds.get(1).equals("JWsOb4iV7rYti9cSSqJgPjygxEK2"))
                    messageButton.setVisibility(View.VISIBLE);

                imageView = (ImageView) fragmentView.findViewById(R.id.image_game2);
                mediaReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        if (getContext() != null && !((Activity) getContext()).isFinishing()) {
                            Glide.with(getContext()).load(uri.toString())
                                    .thumbnail(0.3f)
                                    .override(200, 200)
                                    .crossFade()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(new GlideDrawableImageViewTarget(imageView) {
                                        @Override
                                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                            super.onResourceReady(resource, animation);
                                            progressBar.setVisibility(View.INVISIBLE);
                                            boolean hasVoted = false;
                                            boolean hasVoted2 = false;
                                            if (game.voters2 != null) {
                                                for (String user : voters) {
                                                    if (user.equals(firebaseAuth.getCurrentUser().getUid())) {
                                                        hasVoted = true;
                                                    }
                                                }
                                            }
                                            if (game.voters1 != null) {
                                                for (String user : game.voters1) {
                                                    if (user.equals(firebaseAuth.getCurrentUser().getUid())) {
                                                        hasVoted2 = true;
                                                    }
                                                }
                                            }

                                            if (hasVoted) {
                                                voteButton.setVisibility(View.VISIBLE);
                                                voteButton.setEnabled(false);
                                                voteButton.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                                                voteButton.setText("Voted");
                                            } else {
                                                if (hasVoted2) {
                                                    voteButton.setVisibility(View.GONE);
                                                } else {
                                                    voteButton.setVisibility(View.VISIBLE);
                                                    voteButton.setEnabled(true);
                                                }
                                            }
                                            voteButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    voteButton.setEnabled(false);
                                                    voteButton.setSupportBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                                                    voteButton.setText("Voted");
                                                    Button voteButton2 = (Button) container.findViewById(R.id.button_vote_game1);
                                                    voteButton2.setVisibility(View.GONE);
                                                    voters.add(firebaseAuth.getCurrentUser().getUid());
                                                    gameValues.put("voters2", voters);
                                                    database.child("games").child(getActivity().getIntent().getStringExtra("gameId")).updateChildren(gameValues, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            Notification notification = new Notification(game.playerIds.get(1), "Someone has voted for your photo");
                                                            database.child("notifications").push().setValue(notification);
                                                            MyApplication.getInstance().sendFCM(game.players.get(1).fcmId, "Someone has voted for your photo");
                                                        }
                                                    });
                                                }
                                            });

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
                progressBar.setVisibility(View.INVISIBLE);
                Snackbar.make(imageView, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        return fragmentView;
    }

}
