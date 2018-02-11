package com.dipitize.app.dipitize.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
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
import com.dipitize.app.dipitize.activity.AccountActivity;
import com.dipitize.app.dipitize.activity.GameActivity;
import com.dipitize.app.dipitize.model.Game;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class GamesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Game> games = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public GamesRecyclerAdapter(Context context, List<Game> games, List<String> ids) {
        this.context = context;
        this.games = games;
        this.ids = ids;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final ItemHolder itemHolder = (ItemHolder) holder;

        itemHolder.textHelp.setVisibility(View.VISIBLE);

        final Game game = games.get(position);

        if (game.playerIds.get(0).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                || game.playerIds.get(1).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            itemHolder.shareButton.setVisibility(View.VISIBLE);
        }

        List<String> voters1 = game.voters1;
        List<String> voters2 = game.voters2;

//        final String[] txtVoters1 = {""};
        if (voters1 != null) {
            itemHolder.textVoters1.setText(String.valueOf(voters1.size()) + " Votes");
//            final int votersLength = voters1.size();
//            for (int i = 0; i < voters1.size(); i++) {
//                String voter = voters1.get(i);
//                final int finalI = i;
//                FirebaseDatabase.getInstance().getReference().child("users").child(voter).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        txtVoters1[0] += "@" + dataSnapshot.getValue(User.class).username + "\n";
//                        if (votersLength == finalI + 1) {
//                            itemHolder.textVoters1.setText(txtVoters1[0]);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Snackbar.make(itemHolder.textVoters1, "Error Retrieving Voters", Snackbar.LENGTH_LONG).show();
//                    }
//                });
//            }
        } else {
            itemHolder.textVoters1.setText("0 Votes");
        }

//        final String[] txtVoters2 = {""};
        if (voters2 != null) {
            itemHolder.textVoters2.setText(String.valueOf(voters2.size()) + " Votes");
//            final int votersLength = voters2.size();
//            for (int i = 0; i < voters2.size(); i++) {
//                String voter = voters2.get(i);
//                final int finalI = i;
//                FirebaseDatabase.getInstance().getReference().child("users").child(voter).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        txtVoters2[0] += "@" + dataSnapshot.getValue(User.class).username + "\n";
//                        if (votersLength == finalI + 1) {
//                            itemHolder.textVoters2.setText(txtVoters1[0]);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Snackbar.make(itemHolder.textVoters2, "Error Retrieving Voters", Snackbar.LENGTH_LONG).show();
//                    }
//                });
//            }
        } else {
            itemHolder.textVoters2.setText("0 Votes");
        }

        User user1 = game.players.get(0);
        User user2 = game.players.get(1);
        final String id = ids.get(position);

        itemHolder.textStatus.setText("Interest: " + game.category);

        itemHolder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitmap = MyApplication.getBitmapFromView(itemHolder.cardView);

                String bitmapPath = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Dipitize Game", "Game between " + game.players.get(0).fullName + " and " + game.players.get(0).fullName);
                Uri uri = Uri.parse(bitmapPath);
                String shareText;
                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(game.playerIds.get(0)))
                    shareText = "Vote @" + game.players.get(0).username + " on Dipitize. Download dipitize on Play Store. #dipitize";
                else {
                    shareText = "Vote @" + game.players.get(1).username + " on Dipitize. Download dipitize on Play Store. #dipitize";
                }
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Dipitize Game Complete");
                intent.putExtra(Intent.EXTRA_TEXT, shareText);
                intent.setType("image/*");
                context.startActivity(Intent.createChooser(intent, "Share Game"));
            }
        });

        itemHolder.textView1.setText("@" + user1.username);
        itemHolder.textView2.setText("@" + user2.username);

        long amount = game.amount;
        itemHolder.textType.setText("N" + amount);
        if (amount == 0)
            itemHolder.textType.setText("Free");
        loadUserImage(itemHolder, game.playerIds.get(0), 0);
        loadUserImage(itemHolder, game.playerIds.get(1), 1);

        itemHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GameActivity.class);
                intent.putExtra("gameId", id);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    private void loadUserImage(final ItemHolder itemHolder, String userId, int userPos) {
        final ImageView imageView;

        if (userPos == 0) {
            imageView = itemHolder.imageView1;
        } else {
            imageView = itemHolder.imageView2;
        }

        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

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
                                        .into(imageView);
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
                                .into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        ImageView imageView1, imageView2;
        TextView textView1, textView2, textType, textVoters1, textVoters2, textStatus, textHelp;
        Button shareButton;
        CardView cardView;

        ItemHolder(View view) {
            super(view);

            imageView1 = (ImageView) view.findViewById(R.id.image_game_profile1);
            imageView2 = (ImageView) view.findViewById(R.id.image_game_profile2);
            textView1 = (TextView) view.findViewById(R.id.text_player_1);
            textView2 = (TextView) view.findViewById(R.id.text_player_2);
            textVoters1 = (TextView) view.findViewById(R.id.text_voters1);
            textVoters2 = (TextView) view.findViewById(R.id.text_voters2);
            textType = (TextView) view.findViewById(R.id.text_game_life);
            textStatus = (TextView) view.findViewById(R.id.text_game_status);
            textHelp = (TextView) view.findViewById(R.id.text_help);
            shareButton = (Button) view.findViewById(R.id.btn_share_result);
            cardView = (CardView) view.findViewById(R.id.layout_item_game);
        }
    }
}
