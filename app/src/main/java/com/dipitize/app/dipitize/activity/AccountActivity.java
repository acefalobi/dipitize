package com.dipitize.app.dipitize.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.User;
import com.dipitize.app.dipitize.model.WithdrawRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

import static com.dipitize.app.dipitize.activity.PlayActivity.UPLOAD_IMAGE;

public class AccountActivity extends AppCompatActivity {

    AppCompatButton /*btnApplyForJudge,*/ btnGotoFundAccount, btnBlockUser, btnWithdraw;
    TextView txtDisplayName, txtUsername, txtBalance, txtEmail, txtPhoneNumber, txtAccountNumber, txtBankName;
    ImageView imageView;
    CardView cardView;

    ProgressDialog mProgressDialog;

    User user;

    Map<String, Object> userValues;

    FirebaseAuth firebaseAuth;
    FirebaseStorage storage;

    DatabaseReference database;
    DatabaseReference userReference;

    StorageReference mStorageReference;
    StorageReference avatarReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up ProgressDialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Uploading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);

        storage = FirebaseStorage.getInstance();

        mStorageReference = storage.getReferenceFromUrl("gs://dipitize.appspot.com");

        avatarReference = mStorageReference.child("Avatars");

        txtDisplayName = (TextView) findViewById(R.id.account_display_name);
        txtUsername = (TextView) findViewById(R.id.account_username);
        txtBalance = (TextView) findViewById(R.id.text_balance);
        txtEmail = (TextView) findViewById(R.id.account_email);
        txtPhoneNumber = (TextView) findViewById(R.id.account_phone_number);
        txtBankName = (TextView) findViewById(R.id.account_bank_name);
        txtAccountNumber = (TextView) findViewById(R.id.account_account_number);
        imageView = (ImageView) findViewById(R.id.image_account_profile);
        cardView = (CardView) findViewById(R.id.card_balance);
        btnBlockUser = (AppCompatButton) findViewById(R.id.btn_block_user);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();
        userReference = database.child("users").child(getIntent().getStringExtra("userId"));
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                userValues = user.toMap();

                if (!getIntent().getStringExtra("userId").equals(firebaseAuth.getCurrentUser().getUid())) {
                    txtEmail.setVisibility(View.GONE);
                    txtPhoneNumber.setVisibility(View.GONE);
                    txtAccountNumber.setVisibility(View.GONE);
                    txtBankName.setVisibility(View.GONE);
                    cardView.setVisibility(View.GONE);
//                    btnApplyForJudge.setVisibility(View.GONE);
                } else {
//                    btnApplyForJudge.setVisibility(View.VISIBLE);
                    cardView.setVisibility(View.VISIBLE);
                }

                if (firebaseAuth.getCurrentUser().getUid().equals("JWsOb4iV7rYti9cSSqJgPjygxEK2")) {
                    txtEmail.setVisibility(View.VISIBLE);
                    txtPhoneNumber.setVisibility(View.VISIBLE);
                    txtAccountNumber.setVisibility(View.VISIBLE);
                    txtBankName.setVisibility(View.VISIBLE);
                    cardView.setVisibility(View.VISIBLE);
                    btnBlockUser.setVisibility(View.VISIBLE);
                    btnGotoFundAccount.setVisibility(View.GONE);
                    btnWithdraw.setVisibility(View.GONE);
                }

                btnWithdraw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (user.accountType == null) {
                            LayoutInflater layoutInflater = LayoutInflater.from(AccountActivity.this);
                            final View dialogView = layoutInflater.inflate(R.layout.dialog_account, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                                    .setView(dialogView)
                                    .setCancelable(false)
                                    .setTitle("Complete Account Details")
                                    .setPositiveButton("Confirm", null)
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(final DialogInterface dialog) {
                                    Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                                    button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            final String accountNumber = ((EditText) dialogView.findViewById(R.id.sign_up_account_number)).getText().toString();
                                            final String bankName = ((EditText) dialogView.findViewById(R.id.sign_up_bank_name)).getText().toString();
                                            final String accountType = ((EditText) dialogView.findViewById(R.id.sign_up_account_type)).getText().toString();
                                            if (accountNumber.equals("")) {
                                                Toast.makeText(AccountActivity.this, "Enter an account number", Toast.LENGTH_SHORT).show();
                                            } else if (bankName.equals("")) {
                                                Toast.makeText(AccountActivity.this, "Enter a bank name", Toast.LENGTH_SHORT).show();
                                            } else if (accountType.equals("")) {
                                                Toast.makeText(AccountActivity.this, "Enter an account type", Toast.LENGTH_SHORT).show();
                                            } else {
                                                dialog.dismiss();
                                                final ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
                                                progressDialog.setMessage("Processing...");
                                                progressDialog.setIndeterminate(true);
                                                progressDialog.setCancelable(false);
                                                progressDialog.show();
                                                userValues.put("accountNumber", accountNumber);
                                                userValues.put("bankName", bankName);
                                                userValues.put("accountType", accountType);
                                                userReference.updateChildren(userValues, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        progressDialog.dismiss();
                                                        LayoutInflater layoutInflater = LayoutInflater.from(AccountActivity.this);
                                                        final View dialogView = layoutInflater.inflate(R.layout.dialog_input, null);
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                                                                .setView(dialogView)
                                                                .setCancelable(false)
                                                                .setTitle("Withdraw")
                                                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                        progressDialog.show();
                                                                        long balance = Long.valueOf(((EditText) dialogView.findViewById(R.id.dialog_input)).getText().toString());
                                                                        if (balance > user.balance) {
                                                                            Snackbar.make(btnWithdraw, "Insufficient balance!", Snackbar.LENGTH_LONG).show();
                                                                            progressDialog.dismiss();
                                                                        } else if (balance < 1000) {
                                                                            Snackbar.make(btnWithdraw, "Withdrawal minimum is N1000!", Snackbar.LENGTH_LONG).show();
                                                                            progressDialog.dismiss();

                                                                        } else {
                                                                            userValues.put("balance", user.balance - balance);
                                                                            WithdrawRequest request = new WithdrawRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), Long.valueOf(((EditText) dialogView.findViewById(R.id.dialog_input)).getText().toString()));
                                                                            database.child("withdrawRequests").push().setValue(request, new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                    progressDialog.dismiss();
                                                                                    if (databaseError == null) {
                                                                                        userReference.updateChildren(userValues, new DatabaseReference.CompletionListener() {
                                                                                            @Override
                                                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                progressDialog.dismiss();
                                                                                                if (databaseError == null) {
                                                                                                    Snackbar.make(btnWithdraw, "Your request has been sent and is been processed", Snackbar.LENGTH_LONG).show();
                                                                                                } else {
                                                                                                    Snackbar.make(btnWithdraw, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    } else {
                                                                                        progressDialog.dismiss();
                                                                                        Snackbar.make(btnWithdraw, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
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
                                        }
                                    });
                                }
                            });
                            alertDialog.show();
                        } else {
                            final ProgressDialog progressDialog = new ProgressDialog(AccountActivity.this);
                            progressDialog.setMessage("Processing...");
                            progressDialog.setIndeterminate(true);
                            progressDialog.setCancelable(false);
                            LayoutInflater layoutInflater = LayoutInflater.from(AccountActivity.this);
                            final View dialogView = layoutInflater.inflate(R.layout.dialog_input, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                                    .setView(dialogView)
                                    .setCancelable(false)
                                    .setTitle("Withdraw")
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            progressDialog.show();
                                            long balance = Long.valueOf(((EditText) dialogView.findViewById(R.id.dialog_input)).getText().toString());
                                            if (balance > user.balance) {
                                                Snackbar.make(btnWithdraw, "Insufficient balance!", Snackbar.LENGTH_LONG).show();
                                                progressDialog.dismiss();
                                            } else if (balance < 1000) {
                                                Snackbar.make(btnWithdraw, "Withdrawal minimum is N1000!", Snackbar.LENGTH_LONG).show();
                                                progressDialog.dismiss();

                                            } else {
                                                userValues.put("balance", user.balance - balance);
                                                WithdrawRequest request = new WithdrawRequest(FirebaseAuth.getInstance().getCurrentUser().getUid(), Long.valueOf(((EditText) dialogView.findViewById(R.id.dialog_input)).getText().toString()));
                                                database.child("withdrawRequests").push().setValue(request, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        progressDialog.dismiss();
                                                        if (databaseError == null) {
                                                            userReference.updateChildren(userValues, new DatabaseReference.CompletionListener() {
                                                                @Override
                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                    progressDialog.dismiss();
                                                                    if (databaseError == null) {
                                                                        Snackbar.make(btnWithdraw, "Your request has been sent and is been processed", Snackbar.LENGTH_LONG).show();
                                                                    } else {
                                                                        Snackbar.make(btnWithdraw, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            progressDialog.dismiss();
                                                            Snackbar.make(btnWithdraw, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
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
                    }
                });

                if (user.isBlocked) {
                    btnBlockUser.setText("Unblock User");
                    btnBlockUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                                    .setCancelable(false)
                                    .setTitle("Unblock User")
                                    .setMessage("Are you sure you want to unblock this user?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            userValues.put("isBlocked", false);
                                            userReference.updateChildren(userValues, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        Snackbar.make(btnBlockUser, "User unblocked successfully", Snackbar.LENGTH_LONG).show();
                                                    } else {
                                                        Snackbar.make(btnBlockUser, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
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
                } else {
                    btnBlockUser.setText("Block User");
                    btnBlockUser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                                    .setCancelable(false)
                                    .setTitle("Block User")
                                    .setMessage("Are you sure you want to block this user?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            userValues.put("isBlocked", true);
                                            userReference.updateChildren(userValues, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError == null) {
                                                        Snackbar.make(btnBlockUser, "User blocked successfully", Snackbar.LENGTH_LONG).show();
                                                    } else {
                                                        Snackbar.make(btnBlockUser, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
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

                txtDisplayName.setText(user.fullName);
                txtUsername.setText(user.username);
                txtBalance.setText("N" + user.balance);
                txtEmail.setText("Email: " + user.email);
                txtPhoneNumber.setText("Phone Number: " + user.phoneNumber);
                if (user.bankName != null) {
                    txtBankName.setText("Bank Name: " + user.bankName + " (" + user.accountType + ")");
                    txtAccountNumber.setText("Account Number: " + user.accountNumber);
                } else {
                    txtBankName.setText("Bank Name: None");
                    txtAccountNumber.setText("Account Number: None");
                }

                if (user.profilePictureLink == null) {
                    if (getApplicationContext() != null && !AccountActivity.this.isFinishing()) {
                        FirebaseStorage.getInstance().getReferenceFromUrl("gs://dipitize.appspot.com").child("Avatars").child("default_avatar.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(getApplicationContext()).load(uri.toString())
                                        .thumbnail(0.5f)
                                        .override(200, 200)
                                        .crossFade()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(imageView);
                            }
                        });
                    }
                } else {
                    if (getApplicationContext() != null && !AccountActivity.this.isFinishing()) {
                        Glide.with(getApplicationContext()).load(user.profilePictureLink)
                                .thumbnail(0.5f)
                                .override(200, 200)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(imageView);
                    }
                }

                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                                .setCancelable(false)
                                .setTitle("Change Profile Picture")
                                .setMessage("Are you sure you want to change your profile picture?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_PICK);
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(intent, "Select Image"), UPLOAD_IMAGE);
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

//                btnApplyForJudge.setEnabled(true);
//                if (user.appliedForJudge) {
//                    btnApplyForJudge.setText("Applied For Judge");
//                    btnApplyForJudge.setEnabled(false);
//                } else {
//                    btnApplyForJudge.setText("Apply For Judge (N10)");
//                    btnApplyForJudge.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
//                                    .setCancelable(false)
//                                    .setTitle("Apply for judge")
//                                    .setMessage("Are you sure you want to apply for judge? (You will be charged N10)")
//                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            if (user.balance > 10) {
//                                                userValues.put("appliedForJudge", true);
//                                                userValues.put("balance", user.balance - 10);
//                                                userReference.updateChildren(userValues, new DatabaseReference.CompletionListener() {
//                                                    @Override
//                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                        if (databaseError == null) {
//                                                            Snackbar.make(btnApplyForJudge, "Application Successful", Snackbar.LENGTH_LONG).show();
//                                                        } else {
//                                                            Snackbar.make(btnApplyForJudge, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
//                                                        }
//                                                    }
//                                                });
//                                            } else {
//                                                Snackbar.make(btnApplyForJudge, "Insufficient funds", Snackbar.LENGTH_LONG).show();
//                                            }
//                                        }
//                                    })
//                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialogInterface, int i) {
//                                            dialogInterface.cancel();
//                                        }
//                                    });
//                            AlertDialog alertDialog = builder.create();
//                            alertDialog.show();
//                        }
//                    });
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(cardView, databaseError.toException().getMessage(),Snackbar.LENGTH_LONG).show();

                if (firebaseAuth.getCurrentUser().getUid().equals("JWsOb4iV7rYti9cSSqJgPjygxEK2")) {
                    Intent home_intent = new Intent(AccountActivity.this, AdminActivity.class);
                    home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(home_intent);
                    finish();
                } else {
                    Intent home_intent = new Intent(AccountActivity.this, HomeActivity.class);
                    home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(home_intent);
                    finish();
                }
            }
        });

        btnWithdraw = (AppCompatButton) findViewById(R.id.button_goto_withdraw_account);

//        btnApplyForJudge = (AppCompatButton) findViewById(R.id.btn_apply_for_judge);
        btnGotoFundAccount = (AppCompatButton) findViewById(R.id.button_goto_fund_account);
        btnGotoFundAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AccountActivity.this, FundAccountActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!getIntent().getStringExtra("userId").equals(firebaseAuth.getCurrentUser().getUid()) && !firebaseAuth.getCurrentUser().getUid().equals("JWsOb4iV7rYti9cSSqJgPjygxEK2")) {
            return true;
        } else {
            getMenuInflater().inflate(R.menu.menu_account, menu);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (firebaseAuth.getCurrentUser().getUid().equals("JWsOb4iV7rYti9cSSqJgPjygxEK2")) {
                Intent home_intent = new Intent(this, AdminActivity.class);
                home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(home_intent);
                finish();
                return true;
            } else {
                Intent home_intent = new Intent(this, HomeActivity.class);
                home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(home_intent);
                finish();
                return true;
            }
        } else if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("Dipitize is a photo contest where friends challenge each other for likes and money")
                    .setCancelable(true)
                    .setTitle("About Dipitize");
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        } else if (id == R.id.action_inbox_admin) {
            Intent intent = new Intent(this, InboxActivity.class);
            intent.putExtra("userId", getIntent().getStringExtra("userId"));
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == UPLOAD_IMAGE) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this)
                        .setCancelable(false)
                        .setTitle("Upload Profile Photo")
                        .setMessage("Are you sure you want to change your profile photo")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mProgressDialog.show();
                                Uri selectedImageUri = data.getData();

                                ContentResolver contentResolver = getContentResolver();
                                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

                                String type = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(selectedImageUri));

                                StorageReference storageReference = avatarReference.child(String.valueOf(System.currentTimeMillis()) + "." + type);

                                uploadMedia(storageReference, selectedImageUri);
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
        }
    }

    private void uploadMedia(final StorageReference storageReference, Uri selectedImageUri) {
        UploadTask uploadTask = storageReference.putFile(selectedImageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(imageView, e.getMessage(),Snackbar.LENGTH_LONG).show();
                mProgressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.child("users").child(getIntent().getStringExtra("userId")).child("profilePictureLink").setValue(uri.toString());
                        mProgressDialog.dismiss();
                    }
                });
            }
        });
    }
}
