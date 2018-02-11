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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.MyApplication;
import com.dipitize.app.dipitize.model.Challenge;
import com.dipitize.app.dipitize.model.Notification;
import com.dipitize.app.dipitize.model.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    static final int UPLOAD_IMAGE = 1000;
    static final int UPLOAD_VIDEO = 1001;

    FirebaseStorage storage;
    FirebaseAuth firebaseAuth;

    StorageReference mStorageReference, imagesReference, videosReference;

    DatabaseReference database;
    DatabaseReference challenges;
    DatabaseReference users;

    User currentUser;

    Spinner spinnerCategory;
    Spinner spinnerAmount;

    Button buttonCreateChallenge;
//    RadioButton radioImage, radioVideo;

    ProgressDialog progressDialog;

    String selectedCategory;
    int selectedAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance().getReference();
        challenges = database.child("challenges");

        users = database.child("users").child(firebaseAuth.getCurrentUser().getUid());

        storage = FirebaseStorage.getInstance();

        mStorageReference = storage.getReferenceFromUrl("gs://dipitize.appspot.com");

        imagesReference = mStorageReference.child("Images");
        videosReference = mStorageReference.child("Videos");

        // Set up ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        spinnerCategory = (Spinner) findViewById(R.id.spinner_category);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedCategory = "#Funny";
                        break;
                    case 1:
                        selectedCategory = "#Fashion";
                        break;
                    case 2:
                        selectedCategory = "#Selfie";
                        break;
                    case 3:
                        selectedCategory = "#Inspirational";
                        break;
                    case 4:
                        selectedCategory = "#Photography";
                        break;
                    case 5:
                        selectedCategory = "#MakeUp";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAmount = (Spinner) findViewById(R.id.spinner_amount);
        spinnerAmount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        selectedAmount = 0;
                        break;
                    case 1:
                        selectedAmount = 50;
                        break;
                    case 2:
                        selectedAmount = 500;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        List<String> categories = new ArrayList<>();
        categories.add("#Funny");
        categories.add("#Fashion");
        categories.add("#Selfie");
        categories.add("#Inspirational");
        categories.add("#Photography");
        categories.add("#MakeUp");

        List<String> amounts = new ArrayList<>();
        amounts.add("Honor (Free)");
        amounts.add("N50");
        amounts.add("N500");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(adapter);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, amounts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAmount.setAdapter(adapter);

//        radioImage = (RadioButton) findViewById(R.id.radio_image);
//        radioVideo = (RadioButton) findViewById(R.id.radio_video);

        buttonCreateChallenge = (Button) findViewById(R.id.button_create_challenge);
        buttonCreateChallenge.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == UPLOAD_IMAGE) {
                progressDialog.show();
                Uri selectedImageUri = data.getData();

                ContentResolver contentResolver = getContentResolver();
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

                String type = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(selectedImageUri));

                StorageReference storageReference = imagesReference.child(String.valueOf(System.currentTimeMillis()) + "." + type);

                uploadMedia(storageReference, selectedImageUri);
            }
            if (requestCode == UPLOAD_VIDEO) {
                progressDialog.show();
                Uri selectedImageUri = data.getData();

                ContentResolver contentResolver = getContentResolver();
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

                String type = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(selectedImageUri));

                StorageReference storageReference = videosReference.child(String.valueOf(System.currentTimeMillis()) + "." + type);

                uploadMedia(storageReference, selectedImageUri);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_create_challenge) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Play Game")
                    .setMessage("Are you sure you want to upload an image and play this game")
                    .setCancelable(false)
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
    }

    private void uploadMedia(final StorageReference storageReference, Uri uri) {
        UploadTask uploadTask = storageReference.putFile(uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(buttonCreateChallenge, e.getMessage(),Snackbar.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.setMessage("Creating challenge...");
                createChallenge(storageReference);
            }
        });
    }

    private void createChallenge(final StorageReference storageReference) {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Map<String, Object> userValues = currentUser.toMap();
                if (currentUser.balance >= selectedAmount) {
                    userValues.put("balance", currentUser.balance - selectedAmount);
                    users.updateChildren(userValues, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                Challenge challenge = new Challenge(selectedAmount, storageReference.getName(), FirebaseAuth.getInstance().getCurrentUser().getUid(), currentUser, selectedCategory);
                                challenges.push().setValue(challenge);
                                progressDialog.dismiss();
                                Notification notification = new Notification(dataSnapshot.getKey(), "Your photo has been successfully uploaded for the contest, your game starts soon");
                                database.child("notifications").push().setValue(notification);
                                MyApplication.getInstance().sendFCM(currentUser.fcmId, "Your photo has been successfully uploaded for the contest, your game starts soon");
                                Toast.makeText(PlayActivity.this, "Match-up Process Started", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PlayActivity.this, HomeActivity.class));
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Snackbar.make(buttonCreateChallenge, databaseError.toException().getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Snackbar.make(buttonCreateChallenge, "Insufficient funds", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Snackbar.make(buttonCreateChallenge, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent home_intent = new Intent(this, HomeActivity.class);
            home_intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(home_intent);
            finish();
            return  true;
        }

        return super.onOptionsItemSelected(item);
    }
}