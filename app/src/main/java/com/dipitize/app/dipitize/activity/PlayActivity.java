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

import com.countritv.app.countritv.R;
import com.dipitize.app.dipitize.model.Challenge;
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

public class PlayActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

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
    Button buttonCreateChallenge;
//    RadioButton radioImage, radioVideo;

    ProgressDialog progressDialog;
    int selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);
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
        spinnerCategory.setOnItemSelectedListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("N50 / 30 min");
        categories.add("N500 / 5 hours");
        categories.add("N1000 / 10 hours");
        categories.add("N5000 / 24 hours");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategory.setAdapter(adapter);

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
                    .setTitle("Accept Challenge")
                    .setMessage("Are you sure you want to upload an image and create this challenge")
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
//            if (radioImage.isChecked()) {
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent, "Select Image"), UPLOAD_IMAGE);
//            }
//            else if (radioVideo.isChecked()) {
//            }
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
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Challenge challenge = new Challenge(selectedCategory, storageReference.getName(), currentUser, false);
                challenges.push().setValue(challenge);
                progressDialog.dismiss();
                Snackbar.make(buttonCreateChallenge, "Challenge Created",Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Snackbar.make(buttonCreateChallenge, "Error retrieving data", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selectedCategory = i + 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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