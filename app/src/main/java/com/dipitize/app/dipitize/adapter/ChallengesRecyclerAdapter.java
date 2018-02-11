package com.dipitize.app.dipitize.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
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
import com.dipitize.app.dipitize.activity.ChallengeActivity;
import com.dipitize.app.dipitize.activity.MatchChallengeActivity;
import com.dipitize.app.dipitize.model.Challenge;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class ChallengesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Challenge> challenges = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public ChallengesRecyclerAdapter(Context context, List<Challenge> challenges, List<String> ids) {
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ItemHolder itemHolder = (ItemHolder) holder;

        final Challenge challenge = challenges.get(position);
        final String id = ids.get(position);

        itemHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChallengeActivity.class);
                intent.putExtra("challengeId", id);
                context.startActivity(intent);
            }
        });

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
                gotoMatchChallenge(challenge, id);
            }
        });
    }

    private void gotoMatchChallenge(Challenge challenge, String id) {
        MyApplication.getInstance().selectedChallenge1 = challenge;
        MyApplication.getInstance().challengeKey1 = id;
        Intent intent = new Intent(context, MatchChallengeActivity.class);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView, textView2;
        Button button;
        CardView cardView;

        ItemHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.image_challenge_profile);
            textView = (TextView) view.findViewById(R.id.text_challenge_type);
            textView2 = (TextView) view.findViewById(R.id.text_challenge_category);
            button = (Button) view.findViewById(R.id.btn_challenge_match);
            cardView = (CardView) view.findViewById(R.id.layout_item_challenge);
        }
    }
}
