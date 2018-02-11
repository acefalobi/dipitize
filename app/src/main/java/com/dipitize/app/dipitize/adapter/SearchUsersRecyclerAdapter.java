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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.activity.AccountActivity;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class SearchUsersRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<User> users = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public SearchUsersRecyclerAdapter(Context context, List<User> users, List<String> ids) {
        this.context = context;
        this.users = users;
        this.ids = ids;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final ItemHolder itemHolder = (ItemHolder) holder;

        final User user = users.get(position);
        final String id = ids.get(position);

        itemHolder.textViewDisplayName.setText(user.fullName);
        itemHolder.textViewUsername.setText(user.username);

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
                                .into(itemHolder.imageView);
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
                        .into(itemHolder.imageView);
            }
        }

        itemHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AccountActivity.class);
                intent.putExtra("userId", id);
                context.startActivity(intent);
                ((Activity) context).finish();
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewDisplayName, textViewUsername;
        CardView cardView;

        ItemHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.image_user_search_profile);
            textViewDisplayName = (TextView) view.findViewById(R.id.text_user_search_display_name);
            textViewUsername = (TextView) view.findViewById(R.id.text_user_search_username);

            cardView = (CardView) view.findViewById(R.id.layout_item_user);
        }
    }
}
