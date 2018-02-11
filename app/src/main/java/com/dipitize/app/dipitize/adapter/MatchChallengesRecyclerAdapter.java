package com.dipitize.app.dipitize.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.activity.AdminActivity;
import com.dipitize.app.dipitize.activity.ChooseJudgesActivity;
import com.dipitize.app.dipitize.model.Challenge;
import com.dipitize.app.dipitize.model.Game;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MatchChallengesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Challenge> challenges = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public MatchChallengesRecyclerAdapter(Context context, List<Challenge> challenges, List<String> ids) {
        this.context = context;
        this.challenges = challenges;
        this.ids = ids;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemHolder itemHolder = (ItemHolder) holder;
        itemHolder.button.setText("SELECT");
        final Challenge challenge = challenges.get(position);

        FirebaseDatabase.getInstance().getReference().child("users").child(challenge.challengerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User challenger = dataSnapshot.getValue(User.class);

                if (challenger.profilePictureLink == null) {
                    FirebaseStorage.getInstance().getReferenceFromUrl("gs://dipitize.appspot.com").child("Avatars").child("default_avatar.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (context != null && !((Activity) context).isFinishing()) {
                                Glide.with(context).load(uri.toString())
                                        .thumbnail(0.5f)
                                        .override(200, 200)
                                        .crossFade()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into((itemHolder).imageView);
                            }
                        }
                    });
                } else {
                    if (context != null && !((Activity) context).isFinishing()) {
                        Glide.with(context).load(challenger.profilePictureLink)
                                .thumbnail(0.5f)
                                .override(200, 200)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(itemHolder.imageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        String price = "N" + challenge.amount;
        if (challenge.amount == 0)
            price = "Free";
        itemHolder.textView.setText("@" + challenge.challenger.username + " - " + price);
        itemHolder.textView2.setText(challenge.category);

        itemHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Match Challenge")
                        .setMessage("Are you sure you want to match @"
                                + MyApplication.getInstance().selectedChallenge1.challenger.username
                                + " with @" + challenge.challenger.username)
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                MyApplication.getInstance().selectedChallenge2 = challenge;
                                MyApplication.getInstance().challengeKey2 = ids.get(holder.getAdapterPosition());

                                final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Starting Game...");
                                progressDialog.setIndeterminate(true);
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                final Challenge challenge1 = MyApplication.getInstance().selectedChallenge1;
                                final String id1 = MyApplication.getInstance().challengeKey1;
                                final Challenge challenge2 = MyApplication.getInstance().selectedChallenge2;
                                final String id2 = MyApplication.getInstance().challengeKey2;

                                List<User> players = new ArrayList<>();
                                players.add(challenge1.challenger);
                                players.add(challenge2.challenger);

                                List<String> playerIds = new ArrayList<>();
                                playerIds.add(challenge1.challengerId);
                                playerIds.add(challenge2.challengerId);

                                List<String> mediaUrls = new ArrayList<>();
                                mediaUrls.add(challenge1.mediaUrl);
                                mediaUrls.add(challenge2.mediaUrl);

                                final Game game = new Game(challenge1.amount, players, playerIds, mediaUrls, challenge1.category);

                                database.child("games").push().setValue(game, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, final DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            databaseReference.child("timeStarted").setValue(ServerValue.TIMESTAMP);
                                            database.child("challenges").child(id1).removeValue();
                                            database.child("challenges").child(id2).removeValue();
                                            Notification notification1 = new Notification(challenge1.challengerId, "You've been matched with @" + challenge2.challenger.username + " and your game has started");
                                            Notification notification2 = new Notification(challenge2.challengerId, "You've been matched with @" + challenge1.challenger.username + " and your game has started");
                                            database.child("notifications").push().setValue(notification1);
                                            database.child("notifications").push().setValue(notification2);

                                            MyApplication.getInstance().sendFCM(challenge1.challenger.fcmId, "You've been matched with @" + challenge2.challenger.username + " and your game has started");
                                            MyApplication.getInstance().sendFCM(challenge2.challenger.fcmId, "You've been matched with @" + challenge1.challenger.username + " and your game has started");
                                            progressDialog.dismiss();
                                            Snackbar.make(itemHolder.button, "Game Started", Snackbar.LENGTH_LONG).show();
                                            context.startActivity(new Intent(context, AdminActivity.class));
                                            ((Activity) context).finish();
                                        } else {
                                            Snackbar.make(itemHolder.button, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }

//    private void matchChallenge(final Challenge challenge, final User id, final Button button) {
//

//    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView, textView2;
        Button button;

        ItemHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.image_challenge_profile);
            textView = (TextView) view.findViewById(R.id.text_challenge_type);
            textView2 = (TextView) view.findViewById(R.id.text_challenge_category);
            button = (Button) view.findViewById(R.id.btn_challenge_match);
        }
    }
}
