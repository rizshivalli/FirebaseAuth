package com.example.princ.firebaseauth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button button;
    EditText email, password;
    TextView textView;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        button = findViewById(R.id.btn1);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        textView = findViewById(R.id.registered);
        button.setOnClickListener(this);
        textView.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);

    }

    private void registerUser() {
        String emailS = email.getText().toString().trim();
        String passwordS = password.getText().toString().trim();

        if (TextUtils.isEmpty(emailS)) {

            //email empty
            Toast.makeText(this, "Email is Empty, Please enter", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(passwordS)) {
            //password empty
            Toast.makeText(this, "Password is empty, Please Enter", Toast.LENGTH_SHORT).show();
            return;
        }
// validation is okay
        progressDialog.setMessage("Registering User.....");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(emailS, passwordS)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //start new activity
                            //user is registered
                            finish();
                            Toast.makeText(MainActivity.this, "User Registered", Toast.LENGTH_SHORT).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(MainActivity.this, "User Already Registered", Toast.LENGTH_SHORT).show();
                            } else {


                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
    }




    @Override
    public void onClick(View v) {
        if (v == button) {
            registerUser();
            finish();

        }
        if (v == textView) {
            //Will open login activity
            finish();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class );
            MainActivity.this.startActivity(intent);

        }
    }

}