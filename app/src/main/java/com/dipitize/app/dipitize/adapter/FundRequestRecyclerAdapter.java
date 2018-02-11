package com.dipitize.app.dipitize.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.model.FundRequest;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FundRequestRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<FundRequest> requests = new ArrayList<>();
    private List<String> ids = new ArrayList<>();

    public FundRequestRecyclerAdapter(Context context, List<FundRequest> requests, List<String> ids) {
        this.context = context;
        this.requests = requests;
        this.ids = ids;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fund_request, parent, false);
        return new FundRequestRecyclerAdapter.ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ItemHolder itemHolder = (ItemHolder) holder;

        final FundRequest request = requests.get(position);
        final String requestId = ids.get(position);

        FirebaseDatabase.getInstance().getReference().child("users").child(request.user).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                final Map<String, Object> userValues = user.toMap();

                itemHolder.textDisplayName.setText(user.fullName);
                itemHolder.textUsername.setText("@" + user.username);
                itemHolder.textAmount.setText("Amount: " + request.amount);

                itemHolder.btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        LayoutInflater layoutInflater = LayoutInflater.from(context);
                        final View dialogView = layoutInflater.inflate(R.layout.dialog_input, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setView(dialogView)
                                .setCancelable(false)
                                .setTitle("Credit Account")
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        final ProgressDialog progressDialog = new ProgressDialog(context);
                                        progressDialog.setMessage("Crediting Account...");
                                        progressDialog.setIndeterminate(true);
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        long balance = user.balance + Long.valueOf(((EditText) dialogView.findViewById(R.id.dialog_input)).getText().toString());
                                        userValues.put("balance", balance);
                                        FirebaseDatabase.getInstance().getReference().child("users").child(request.user).updateChildren(userValues, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Notification notification = new Notification(request.user, "Your credit request of N" + request.amount + " has been accepted");
                                                    FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);
                                                    MyApplication.getInstance().sendFCM(user.fcmId, "Your credit request of N" + request.amount + " has been accepted");
                                                    FirebaseDatabase.getInstance().getReference().child("fundRequests").child(ids.get(holder.getAdapterPosition())).removeValue(new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            progressDialog.dismiss();
                                                            if (databaseError == null) {
                                                                Snackbar.make(itemHolder.btnAccept, "Funding request accepted", Snackbar.LENGTH_LONG).show();
                                                            } else {
                                                                Snackbar.make(itemHolder.btnAccept, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    progressDialog.dismiss();
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

                itemHolder.btnDecline.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setMessage("Are you sure you want to decline this request?")
                                .setCancelable(false)
                                .setTitle("Decline Request")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseDatabase.getInstance().getReference().child("fundRequests").child(ids.get(holder.getAdapterPosition())).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Notification notification = new Notification(request.user, "Your credit request of N" + request.amount + " has been declined");
                                                    FirebaseDatabase.getInstance().getReference().child("notifications").push().setValue(notification);
                                                    MyApplication.getInstance().sendFCM(user.fcmId, "Your credit request of N" + request.amount + " has been declined");
                                                    Snackbar.make(itemHolder.btnDecline, "Funding request declined", Snackbar.LENGTH_LONG).show();
                                                } else {
                                                    Snackbar.make(itemHolder.btnDecline, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
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
        AppCompatButton btnAccept, btnDecline;

        ItemHolder(View view) {
            super(view);

            textDisplayName = (TextView) view.findViewById(R.id.text_request_display_name);
            textUsername = (TextView) view.findViewById(R.id.text_request_username);
            textAmount = (TextView) view.findViewById(R.id.text_request_amount);
            btnAccept = (AppCompatButton) view.findViewById(R.id.btn_request_accept);
            btnDecline = (AppCompatButton) view.findViewById(R.id.btn_request_decline);
        }
    }
}
