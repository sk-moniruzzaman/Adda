package com.example.adda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {
    private Button updateAccountSetting;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private static final int galleryPick = 1;
    private StorageReference userProfileImageRef;
    private ProgressDialog loadingBar;

    private Toolbar settingToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

        initializeFields();

        userName.setVisibility(View.INVISIBLE);

        updateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSetting();
            }
        });


        retrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });
    }


    private void initializeFields() {
        updateAccountSetting = (Button) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        settingToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Setting");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                final Uri resultUri = result.getUri();
                final StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                final String downloadUrl = uri.toString();
                                rootRef.child("users").child(currentUserId).child("image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingActivity.this, "Profile image stored to firebase database successfully.", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingActivity.this, "Error Occurred..." + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }

                });
            }
        }
    }


    private void updateSetting()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            rootRef.child("users").child(currentUserId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SettingActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }





    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void retrieveUserInfo () {
        rootRef.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                }

                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                    userName.setText(retrieveUserName);
                    userStatus.setText(retrievesStatus);
                }

                else {
                    userName.setVisibility(View.VISIBLE);
                    Toast.makeText(SettingActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}

