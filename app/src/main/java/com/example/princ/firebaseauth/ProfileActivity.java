package com.example.princ.firebaseauth;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101 ;
    ImageView imageView;
EditText editText;
Uri uriProfileImage;
ProgressBar progressBar;
String profileImageUrl;
FirebaseAuth firebaseAuth;
TextView textView;
Toolbar toolbar;
     FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseAuth = FirebaseAuth.getInstance();
        editText= findViewById(R.id.editTextDisplayName);
        imageView = findViewById(R.id.imageView);
        textView=findViewById(R.id.userVerified);
        progressBar= findViewById(R.id.progressBar);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadUserInformation();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChoser();
            }
        });
    findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveUserInformation();
        }
    });

    }

   private void loadUserInformation() {
     final FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(ProfileActivity.this)
                        .load(user.getPhotoUrl().toString())
                        .into(imageView);

            }
            if (user.getDisplayName() != null) {
                editText.setText(user.getDisplayName());

            }
            if(user.isEmailVerified()){
                textView.setText("Email Verified");

            }else{
                textView.setText("Email Not Verified (Click to verify)");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ProfileActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }

            //String photoUrl = user.getPhotoUrl().toString();
           // String displayName = user.getDisplayName();
        }
    }

    private void saveUserInformation() {
        String displayName = editText.getText().toString();
        if(displayName.isEmpty()){
            editText.setError("Name Required");
            editText.requestFocus();
            return;
            }

          user = firebaseAuth.getCurrentUser();
        if (user!=null){
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();
            user.updateProfile(profileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                            else{

                                Toast.makeText(ProfileActivity.this, "", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser()==null){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== CHOOSE_IMAGE && resultCode == RESULT_OK && data!= null && data.getData() != null){
            uriProfileImage= data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);
                uploadImageToFirebaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void uploadImageToFirebaseStorage() {
        StorageReference profileImage = FirebaseStorage.getInstance().getReference("profilePic/"+System.currentTimeMillis() +".jpg");
        progressBar.setVisibility(View.VISIBLE);
        if(uriProfileImage!=null){
            profileImage.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            profileImageUrl= taskSnapshot.getDownloadUrl().toString();

                        }
                    })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId())
        {
            case R.id.menuLogout:
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));

        }

        return true;
    }

    private void showImageChoser(){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Photo"),CHOOSE_IMAGE);

    }
}
