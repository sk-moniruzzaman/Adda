package com.example.adda;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Adda");

        myViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        myTabAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabAccessorAdapter);


        myTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            verifyUserExistence();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            updateUserStatus("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUserId != null){
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistence() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.find_friends){
            sendUserToFindFriendsActivity();
        }

        if(item.getItemId() == R.id.create_group){
            requestNewGroup();
        }

        if(item.getItemId() == R.id.setting){
            sendUserToSettingActivity();
        }

        if(item.getItemId() == R.id.logout){
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        return true;
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDiag);
        builder.setTitle("Enter Group Name: ");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g School friends");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write group name", Toast.LENGTH_SHORT).show();
                }
                else{
                    createNewGroup(groupName);
                }
            }
        });


        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        rootRef.child("groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName + " is created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingActivity() {

        Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(settingIntent);
    }

    private void sendUserToFindFriendsActivity() {

        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("users").child(currentUserId).child("user_state")
                .updateChildren(onlineStateMap);
    }
}

