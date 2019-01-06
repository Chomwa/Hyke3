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

public class CustomerRegistration extends AppCompatActivity {
    private EditText mEmailField, mPassword;

    private Button mRegistration, mLogin;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mCustomerDatabase;
    private DatabaseReference mUsers;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_registration);

        mAuth = FirebaseAuth.getInstance();
        mCustomerDatabase = FirebaseDatabase.getInstance();
        mUsers = mCustomerDatabase.getReference("Users");

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //take user to phone number authentication
                if(user!=null){
                    Intent intent = new Intent(CustomerRegistration.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmailField = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mRegistration = (Button) findViewById(R.id.signup);
    //    mLogin = (Button) findViewById(R.id.login_page);

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RegisterNewCustomerUser();
            }
        });

/*        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //don't need to create a new intent instead finish customer login here
                Intent intent = new Intent(CustomerRegistration.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }); */
    }

    private void RegisterNewCustomerUser() {

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

                            mUsers.child("Customers").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CustomerRegistration.this, "Registration Successful!", Toast.LENGTH_SHORT)
                                                    .show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(CustomerRegistration.this, "Failed: " +e.getMessage(), Toast.LENGTH_SHORT)
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

    //start Auth state Listener
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
