package com.example.adda;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {


    private Button registerButton;
    private EditText registerEmail, registerPassword;
    private TextView alreadyHaveAnAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference rootRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        
        initializeFields();

        alreadyHaveAnAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });
    }


    private void initializeFields() {
        registerButton = (Button) findViewById(R.id.register_button);
        registerEmail = (EditText) findViewById(R.id.register_email);
        registerPassword = (EditText) findViewById(R.id.register_password);
        alreadyHaveAnAccountLink = (TextView) findViewById(R.id.already_have_an_account_link);
        loadingBar = new ProgressDialog(this);
    }


    private void createNewAccount() {
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT);
        }

        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT);
        }
        else{
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait while we are creating new account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                //String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserId = mAuth.getCurrentUser().getUid();
                                rootRef.child("users").child(currentUserId).setValue("");

                                //rootRef.child("users").child(currentUserId).child("device_token").setValue(deviceToken);

                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }







}
