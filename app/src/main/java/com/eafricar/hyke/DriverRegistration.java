package com.eafricar.hyke;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.eafricar.hyke.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverRegistration extends AppCompatActivity {

    private EditText mEmailField, mPassword;

    private Button mRegistration, mLogin;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDriverDatabase;
    private DatabaseReference mUsers;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        mAuth = FirebaseAuth.getInstance();
        mDriverDatabase = FirebaseDatabase.getInstance();
        mUsers = mDriverDatabase.getReference("Users");

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(DriverRegistration.this, PhoneNumberVerification.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmailField = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mRegistration = (Button) findViewById(R.id.signup);
        mLogin = (Button) findViewById(R.id.login_page);


        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                RegisterNewDriverUser();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverRegistration.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }

    private void RegisterNewDriverUser() {

        final String email = mEmailField.getText().toString();
        final String password = mPassword.getText().toString();
        //set errors
        if (email.isEmpty()){
            mEmailField.setError("Email is Required");
            mEmailField.requestFocus();
            return;
        }if (password.isEmpty()) {
            mPassword.setError("Password is Required");
            mPassword.requestFocus();
        }if ((mPassword.getText().toString().length()<6)){
            mPassword.setError("Password should not be less than 6 characters");
            mPassword.requestFocus();
        } else{

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            //Save user to the database
                            User user = new User();
                            user.setEmail(mEmailField.getText().toString());
                            user.setPassword(mPassword.getText().toString());

                            mUsers.child("Drivers").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DriverRegistration.this, "Registration Successful!", Toast.LENGTH_SHORT)
                                                    .show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(DriverRegistration.this, "Failed: " +e.getMessage(), Toast.LENGTH_SHORT)
                                                    .show();

                                        }
                                    });

                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
