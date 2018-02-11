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
import android.widget.Toast;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, FirebaseAuth.AuthStateListener {

    FirebaseAuth firebaseAuth;

    TextInputEditText editEmail, editPassword;
    Button buttonLogin;
    AppCompatTextView textGotoSignUp;

    LoginButton loginButton;

    ProgressDialog progressDialog;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up EditTexts
        editEmail = (TextInputEditText) findViewById(R.id.login_email);
        editPassword = (TextInputEditText) findViewById(R.id.login_password);

        // Set up Buttons
        buttonLogin = (Button) findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(this);
        textGotoSignUp = (AppCompatTextView) findViewById(R.id.button_goto_sign_up);
        textGotoSignUp.setOnClickListener(this);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Toast.makeText(LoginActivity.this, "Facebook Authentication Complete", Toast.LENGTH_SHORT).show();
                progressDialog.show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(LoginActivity.this, "Facebook Authentication Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    private void handleFacebookAccessToken(final AccessToken accessToken) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        final DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        String email = "";
                        try {
                            email = object.getString("email");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        final String finalEmail = email;
                        firebaseDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean isRegistered = false;
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    User user = child.getValue(User.class);
                                    if (user.email.equals(finalEmail)) {
                                        isRegistered = true;
                                    }
                                }
                                if (isRegistered) {
                                    firebaseAuth.signInWithCredential(credential)
                                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (!task.isSuccessful()) {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == buttonLogin.getId()) {
            attemptLogin();
        } else if (view.getId() == textGotoSignUp.getId()) {
            startActivity(new Intent(this, SignUpActivity.class));
        }
    }

    private void attemptLogin() {
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        boolean cancel = false;

        View focusView = null;

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

        if (cancel) {
            focusView.requestFocus();
        } else {
            progressDialog.show();

            login(email, password);
        }
    }

    private void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //noinspection ConstantConditions,ThrowableResultOfMethodCallIgnored
                            progressDialog.dismiss();
                            Snackbar.make(buttonLogin, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
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
            if (firebaseUser.getEmail().equals("admin@dipitize.com")) {
                progressDialog.dismiss();
                startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                finish();
            } else {
                FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user.isBlocked) {
                            progressDialog.dismiss();
                            Snackbar.make(buttonLogin, "Sorry, you've been blocked!", Snackbar.LENGTH_LONG).show();
                        } else {
                            progressDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Snackbar.make(buttonLogin, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
