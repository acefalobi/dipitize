package com.dipitize.app.dipitize.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.activity.AdminActivity;
import com.dipitize.app.dipitize.model.Challenge;
import com.dipitize.app.dipitize.model.Game;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ChooseJudgesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<User> users = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    private List<Boolean> statuses = new ArrayList<>();

    private FloatingActionButton btnSelectJudges;

    public ChooseJudgesRecyclerAdapter(Context context, List<User> users, List<String> ids, FloatingActionButton btnSelectJudges) {
        this.context = context;
        this.users = users;
        this.ids = ids;
        this.btnSelectJudges = btnSelectJudges;

        for (int i = 0; i < users.size(); i++) {
            statuses.add(i, false);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_judge, parent, false);

        btnSelectJudges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<String> judges = new ArrayList<>();
                int noOfJudges = 0;
                for (int i = 0; i < statuses.size(); i++) {
                    if (statuses.get(i)) {
                        noOfJudges++;
                        judges.add(ids.get(i));
                    }
                }
                if (noOfJudges == 11) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setTitle("Start Judges")
                            .setMessage("Are you sure you want to start the match")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
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
                                                database.child("challenges").child(id1).removeValue();
                                                database.child("challenges").child(id2).removeValue();
                                                for (String judge : judges) {
                                                    Notification notification = new Notification(judge, "You've been assigned to a game for judge duty");
                                                    database.child("notifications").push().setValue(notification);
                                                }
                                                Notification notification1 = new Notification(challenge1.challengerId, "You've been matched with @" + challenge2.challenger.username + " and your game has started");
                                                Notification notification2 = new Notification(challenge2.challengerId, "You've been matched with @" + challenge1.challenger.username + " and your game has started");
                                                database.child("notifications").push().setValue(notification1);
                                                database.child("notifications").push().setValue(notification2);
                                                progressDialog.dismiss();
                                                Snackbar.make(btnSelectJudges, "Game Started", Snackbar.LENGTH_LONG).show();
                                                context.startActivity(new Intent(context, AdminActivity.class));
                                            } else {
                                                Snackbar.make(btnSelectJudges, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
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
                } else {
                    Snackbar.make(btnSelectJudges, "Cannot use " + noOfJudges + " judges", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return new ChooseJudgesRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ItemHolder itemHolder = (ItemHolder) holder;
        final User user = users.get(position);

        if (user.profilePictureLink == null) {
            if (context != null && !((Activity) context).isFinishing()) {
                FirebaseStorage.getInstance().getReferenceFromUrl("gs://dipitize.appspot.com").child("Avatars").child("default_avatar.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context.getApplicationContext()).load(uri.toString())
                                .thumbnail(0.5f)
                                .override(200, 200)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into((itemHolder).imageView);
                    }
                });
            }
        } else {
            if (context != null && !((Activity) context).isFinishing()) {
                Glide.with(context.getApplicationContext()).load(user.profilePictureLink)
                        .thumbnail(0.5f)
                        .override(200, 200)
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into((itemHolder).imageView);
            }
        }

        itemHolder.textDisplayNameView.setText(user.fullName);
        itemHolder.textUsernameView.setText("@" + user.username);
        itemHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.wtf("ChooseJudgesRecyclerAdapter", String.valueOf(statuses.size()));
                if (statuses.get(holder.getAdapterPosition())) {
                    itemHolder.button.setText("Select");
                    itemHolder.button.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
                    itemHolder.button.setSupportBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary)));
                    statuses.set(holder.getAdapterPosition(), false);
                } else {
                    itemHolder.button.setText("Unselect");
                    itemHolder.button.setTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.colorPrimary)));
                    itemHolder.button.setSupportBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.white)));
                    statuses.set(holder.getAdapterPosition(), true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textDisplayNameView, textUsernameView;
        AppCompatButton button;

        ItemHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.image_judge_profile);
            textDisplayNameView = (TextView) view.findViewById(R.id.text_judge_display_name);
            textUsernameView = (TextView) view.findViewById(R.id.text_judge_username);
            button = (AppCompatButton) view.findViewById(R.id.btn_select_judge);
        }
    }
}
