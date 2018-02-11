package com.dipitize.app.dipitize.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dipitize.app.dipitize.MyApplication;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.activity.MatchChallengeActivity;
import com.dipitize.app.dipitize.model.Challenge;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class MatchChallengesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int UPLOAD_IMAGE = 1000;

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

        final Challenge challenge = challenges.get(position);

        if (challenge.challenger.profilePictureLink == null) {
            FirebaseStorage.getInstance().getReferenceFromUrl("gs://dipitize.appspot.com").child("Avatars").child("default_avatar.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri.toString())
                            .thumbnail(0.5f)
                            .override(200, 200)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into((itemHolder).imageView);
                }
            });
        } else {
            Glide.with(context).load(challenge.challenger.profilePictureLink)
                    .thumbnail(0.5f)
                    .override(200, 200)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into((itemHolder).imageView);
        }
        String price = "N" + challenge.amount;
        itemHolder.textView.setText("@" + challenge.challenger.username + " - " + price);

        itemHolder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                matchChallenge(challenge, ids.get(holder.getAdapterPosition()));
            }
        });
    }

    private void matchChallenge(Challenge challenge, String id) {
        MyApplication.getInstance().selectedChallenge = challenge;
        MyApplication.getInstance().challengeKey = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle("Match Challenge")
                .setMessage("Are you sure you want to match this user with another user")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(context, MatchChallengeActivity.class));
//                        Intent intent = new Intent(Intent.ACTION_PICK);
//                        intent.setType("image/*");
//                        intent.setAction(Intent.ACTION_GET_CONTENT);
//                        ((Activity) context).startActivityForResult(Intent.createChooser(intent, "Select Image"), UPLOAD_IMAGE);
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

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        Button button;

        ItemHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.image_challenge_profile);
            textView = (TextView) view.findViewById(R.id.text_challenge_type);
            button = (Button) view.findViewById(R.id.btn_challenge_match);
        }
    }
}
