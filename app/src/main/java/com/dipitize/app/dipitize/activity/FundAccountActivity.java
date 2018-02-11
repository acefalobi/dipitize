package com.dipitize.app.dipitize.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.FundRequest;
import com.dipitize.app.dipitize.model.User;
import com.dipitize.app.dipitize.model.WithdrawRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FundAccountActivity extends AppCompatActivity {

    TextView txtAmount;

    Button buttonFundAccount;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fund_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference fundRequestsReference = database.child("fundRequests");

        txtAmount = (TextView) findViewById(R.id.fund_pin);

        // Set up ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processing...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        buttonFundAccount = (Button) findViewById(R.id.button_fund_account);
        buttonFundAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtAmount.getText().toString().isEmpty()) {
                    txtAmount.setError("Amount cannot be empty");
                    txtAmount.requestFocus();
                } else {
                    progressDialog.show();
                    FundRequest fundRequest = new FundRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), txtAmount.getText().toString());
                    fundRequestsReference.push().setValue(fundRequest, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            progressDialog.dismiss();
                            if (databaseError == null) {
                                Snackbar.make(buttonFundAccount, "Your request has been sent and is been processed", Snackbar.LENGTH_LONG).show();
                                Intent intent = new Intent(FundAccountActivity.this, AccountActivity.class);
                                intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                startActivity(intent);
                                finish();
                            } else {
                                Snackbar.make(buttonFundAccount, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent home_intent = new Intent(this, AccountActivity.class);
            home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(home_intent);
            finish();
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }
}
