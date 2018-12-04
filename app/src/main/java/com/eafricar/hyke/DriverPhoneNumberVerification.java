package com.eafricar.hyke;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class DriverPhoneNumberVerification extends AppCompatActivity {

    private EditText editTextPhone, editTextCode;

    private FirebaseAuth mAuth;

    private String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_phone_number_verification);

        mAuth = FirebaseAuth.getInstance();

        editTextPhone = findViewById(R.id.phonenumber);
        editTextCode = findViewById(R.id.verificationcode);

        findViewById(R.id.verificationbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode();

            }
        });

        findViewById(R.id.signinbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();

            }
        });


    }

    private void verifySignInCode(){


        String code = editTextCode.getText().toString();

        if (code.isEmpty()){
            editTextCode.setError("Verification Code is Required");
            editTextCode.requestFocus();
            return;
        }else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(!task.isSuccessful()){
                            Toast.makeText(DriverPhoneNumberVerification.this, "sign up error", Toast.LENGTH_SHORT).show();}

                        if (task.isSuccessful()) {

                            Toast.makeText(getApplicationContext(),
                                    "Login Successful", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(DriverPhoneNumberVerification.this, RegistrationDriverProfile.class);
                            startActivity(intent);
                            finish();
                            return;
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Login Successful", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(DriverPhoneNumberVerification.this, RegistrationDriverProfile.class);
                                startActivity(intent);
                                finish();
                                return;

                            }
                        }
                    }
                });
    }



    private void sendVerificationCode(){

        String phoneNumber = editTextPhone.getText().toString();

        if (phoneNumber.isEmpty()){
            editTextPhone.setError("Phone Number is Required");
            editTextPhone.requestFocus();
            return;
        }
        if (phoneNumber.length() < 10) {
            editTextPhone.setError("Enter a Valid Phone Number");
            editTextPhone.requestFocus();
            return;
        }


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            codeSent = s;
        }
    };
}
