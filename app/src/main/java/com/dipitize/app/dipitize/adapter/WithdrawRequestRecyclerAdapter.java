package com.dipitize.app.dipitize.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.dipitize.app.dipitize.model.WithdrawRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Made by acefalobi on 3/16/2017.
 */

public class WithdrawRequestRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<WithdrawRequest> requests = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public WithdrawRequestRecyclerAdapter(Context context, List<WithdrawRequest> requests, List<String> ids) {
        this.context = context;
        this.requests = requests;
        this.ids = ids;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_withdraw_request, parent, false);
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final ItemHolder itemHolder = (ItemHolder) holder;

        final WithdrawRequest request = requests.get(position);
        final String requestId = ids.get(position);

        FirebaseDatabase.getInstance().getReference().child("users").child(request.user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);

                itemHolder.textDisplayName.setText(user.fullName);
                itemHolder.textUsername.setText("@" + user.username);
                itemHolder.textAmount.setText("Amount: N" + request.amount);

                itemHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setMessage("Are you sure you want to confirm this request?")
                                .setCancelable(false)
                                .setTitle("Confirm Request")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseDatabase.getInstance().getReference().child("withdrawRequests").child(requestId).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Notification notification = new Notification(request.user, "Your withdraw request of N" + request.amount + " has been confirmed");
                                                    FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);
                                                    MyApplication.getInstance().sendFCM(user.fcmId, "Your withdraw request of N" + request.amount + " has been confirmed");
                                                    Snackbar.make(itemHolder.btnAccept, "Request confirmed", Snackbar.LENGTH_LONG).show();
                                                } else {
                                                    Snackbar.make(itemHolder.btnAccept, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(itemHolder.itemView, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG);
            }
        });

    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        TextView textDisplayName, textUsername, textAmount;
        AppCompatButton btnAccept;

        ItemHolder(View view) {
            super(view);
            textAmount = (TextView) view.findViewById(R.id.text_request_amount);
            textDisplayName = (TextView) view.findViewById(R.id.text_request_display_name);
            textUsername = (TextView) view.findViewById(R.id.text_request_username);
            btnAccept = (AppCompatButton) view.findViewById(R.id.btn_request_accept);
        }
    }
}
