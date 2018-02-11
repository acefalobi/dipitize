package com.dipitize.app.dipitize.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.activity.ChatActivity;
import com.dipitize.app.dipitize.model.DMThread;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

/**
 * Made by acefalobi on 5/10/2017.
 */

public class MessageThreadRecyclerAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<DMThread> messageThreads;
    private List<String> messageThreadIds;

    public MessageThreadRecyclerAdapter(Context context, List<DMThread> messageThreads, List<String> messageThreadIds) {
        this.context = context;
        this.messageThreads = messageThreads;
        this.messageThreadIds = messageThreadIds;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dm_thread, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            final ItemHolder itemHolder = (ItemHolder) holder;
            DMThread messageThread = messageThreads.get(position);
            final String id = messageThreadIds.get(position);
            User user;
            if (messageThread.userId1.equals(firebaseAuth.getCurrentUser().getUid())) {
                user = messageThread.user2;
            } else {
                user = messageThread.user1;
            }
            itemHolder.textUser.setText("@" + user.username);

            if (user.profilePictureLink == null) {
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
                    Glide.with(context).load(user.profilePictureLink)
                            .thumbnail(0.5f)
                            .override(200, 200)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(itemHolder.imageView);
                }
            }
            itemHolder.layoutMatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("dmId",id);
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messageThreads.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {

        LinearLayout layoutMatch;
        TextView textUser;
        ImageView imageView ;

        ItemHolder(View itemView) {
            super(itemView);

            layoutMatch = (LinearLayout) itemView.findViewById(R.id.layout_match);
            textUser = (TextView) itemView.findViewById(R.id.text_dm_user);
            imageView = (ImageView) itemView.findViewById(R.id.image_dm_thread);
        }
    }
}
