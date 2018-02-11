package com.dipitize.app.dipitize.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, FirebaseAuth.AuthStateListener {

    FirebaseAuth firebaseAuth;

    TextInputEditText editUsername, editFullName, editEmail, editPassword, editConfirmPassword, editPhone;
    Button buttonSignUp;
    AppCompatTextView textGotoLogin;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up EditTexts
        editFullName = (TextInputEditText) findViewById(R.id.sign_up_full_name);
        editUsername = (TextInputEditText) findViewById(R.id.sign_up_username);
        editEmail = (TextInputEditText) findViewById(R.id.sign_up_email);
        editPassword = (TextInputEditText) findViewById(R.id.sign_up_password);
        editConfirmPassword = (TextInputEditText) findViewById(R.id.sign_up_confirm_password);
        editPhone = (TextInputEditText) findViewById(R.id.sign_up_phone_number);

        // Set up Buttons
        buttonSignUp = (Button) findViewById(R.id.button_sign_up);
        buttonSignUp.setOnClickListener(this);
        textGotoLogin = (AppCompatTextView) findViewById(R.id.button_goto_login);
        textGotoLogin.setOnClickListener(this);

        // Set up ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing up...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == buttonSignUp.getId()) {
            attemptSignUp();
        } else if (view.getId() == textGotoLogin.getId()) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void attemptSignUp() {
        String username = editUsername.getText().toString();
        String fullName = editFullName.getText().toString();
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();
        String phoneNumber = editPhone.getText().toString();

        boolean cancel = false;

        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Please enter a username");
            focusView = editUsername;
            cancel = true;
        }

        boolean isLegal = false;

        for (int i = 0; i < username.length(); i++) {
            String charAti = String.valueOf(username.charAt(i));
            String pattern = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";

            for (int j = 0; j < pattern.length(); j++) {
                if (String.valueOf(pattern.charAt(j)).equals(charAti)) {
                    isLegal = true;
                    break;
                }
                else {
                    isLegal = false;
                }
            }
        }

        if (!isLegal) {
            editUsername.setError("Allowed characters are a-z, 0-9 and _");
            focusView = editUsername;
            cancel = true;
        }
        if (username.contains(" ") || username.contains(" ")) {
            editUsername.setError("Please enter a valid username");
            focusView = editUsername;
            cancel = true;
        }
        if (TextUtils.isEmpty(fullName)) {
            editFullName.setError("Please enter your full name");
            focusView = editFullName;
            cancel = true;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            editPhone.setError("Please enter a phone number");
            focusView = editPhone;
            cancel = true;
        }
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Please enter an email");
            focusView = editEmail;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Please enter a password");
            focusView = editPassword;
            cancel = true;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            editConfirmPassword.setError("Please re-enter your password");
            focusView = editConfirmPassword;
            cancel = true;
        }
        if (password.length() < 6) {
            editPassword.setError("Password must have at least 6 characters");
            focusView = editPassword;
            cancel = true;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Please re-enter your correct password");
            focusView = editConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            progressDialog.show();
            checkUsername(email, username, password, phoneNumber);
        }
    }

    private void checkUsername(final String email, final String username, final String password, final String phoneNumber) {
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                firebaseAuth.signOut();
                boolean cancel = false;
                for (DataSnapshot user: dataSnapshot.getChildren()) {
                    if (user.getValue(User.class).username.equals(username)) {
                        editUsername.setError("Sorry, this username is taken");
                        editUsername.requestFocus();
                        progressDialog.dismiss();
                        cancel = true;
                    }
                    if (user.getValue(User.class).phoneNumber.equals(phoneNumber)) {
                        editPhone.setError("Sorry, this phone number has been used");
                        editPhone.requestFocus();
                        progressDialog.dismiss();
                        cancel = true;
                    }
                }
                if (!cancel) {
                    progressDialog.dismiss();
                    signUp(email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                firebaseAuth.signOut();
            }
        });
    }

    private void signUp(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //noinspection ConstantConditions,ThrowableResultOfMethodCallIgnored
                            progressDialog.dismiss();
                            Snackbar.make(buttonSignUp, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            User user = new User(firebaseUser.getEmail(), editFullName.getText().toString(), editUsername.getText().toString(), editPhone.getText().toString(), 0);

            database.child("users").child(firebaseUser.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    progressDialog.dismiss();
                    startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                    finish();
                }
            });
        }
    }
}
