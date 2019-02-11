//package com.eafricar.hyke;
//
//import android.app.ProgressDialog;
//import android.util.Log;
//import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.FirebaseException;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
//import com.google.firebase.auth.PhoneAuthCredential;
//import com.google.firebase.auth.PhoneAuthProvider;
//
//import java.util.concurrent.TimeUnit;
//
//public class DriverPhoneNumberVerification extends AppCompatActivity {
//
//    private EditText editTextPhone, editTextCode;
//    private static final String TAG = "Phone number registration";
//
//    private FirebaseAuth mAuth;
//
//    private String codeSent;
//    private ProgressBar pgsBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_driver_phone_number_verification);
//        pgsBar = findViewById(R.id.pBar);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        editTextPhone = findViewById(R.id.phonenumber);
//        editTextCode = findViewById(R.id.verificationcode);
//
//        findViewById(R.id.verificationbutton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sendVerificationCode();
//
//            }
//        });
//
//        findViewById(R.id.signinbutton).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                verifySignInCode();
//
//            }
//        });
//
//
//    }
//
//    private void displayVerficationInput(String display) {
//
//        switch (display) {
//            case "show":
//                findViewById(R.id.phonenumber).setVisibility(View.VISIBLE);
//                findViewById(R.id.verificationbutton).setVisibility(View.VISIBLE);
//                break;
//            case "hide":
//                findViewById(R.id.phonenumber).setVisibility(View.GONE);
//                findViewById(R.id.verificationbutton).setVisibility(View.GONE);
//                break;
//        }
//    }
//
//    private void displayEnterVerficationInput(String display) {
//
//        switch (display) {
//            case "show":
//                findViewById(R.id.verificationcode).setVisibility(View.VISIBLE);
//                findViewById(R.id.signinbutton).setVisibility(View.VISIBLE);
//                break;
//            case "hide":
//                findViewById(R.id.verificationcode).setVisibility(View.GONE);
//                findViewById(R.id.signinbutton).setVisibility(View.GONE);
//                break;
//        }
//    }
//
//    private void verifySignInCode(){
//
//
//        String code = editTextCode.getText().toString();
//
//        if (code.isEmpty()){
//            editTextCode.setError("Verification Code is Required");
//            editTextCode.requestFocus();
//            return;
//        }else {
//            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
//            signInWithPhoneAuthCredential(credential);
//        }
//    }
//
//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//
//                        if(!task.isSuccessful()){
//                            Toast.makeText(DriverPhoneNumberVerification.this, "sign up error", Toast.LENGTH_SHORT).show();}
//
//                        if (task.isSuccessful()) {
//
//                            Toast.makeText(getApplicationContext(),
//                                    "Login Successful", Toast.LENGTH_LONG).show();
//                            Intent intent = new Intent(DriverPhoneNumberVerification.this, RegistrationDriverProfile.class);
//                            startActivity(intent);
//                            finish();
//                            return;
//                        } else {
//                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                Toast.makeText(getApplicationContext(),
//                                        "Login Successful", Toast.LENGTH_LONG).show();
//                                Intent intent = new Intent(DriverPhoneNumberVerification.this, RegistrationDriverProfile.class);
//                                startActivity(intent);
//                                finish();
//                                return;
//
//                            }
//                        }
//                    }
//                });
//    }
//
//
//
//    private void sendVerificationCode(){
//
//        String phoneNumber = "+" + editTextPhone.getText().toString();
//        Log.i(TAG, "Number received" + phoneNumber);
//
//
//        if (phoneNumber.isEmpty()){
//            editTextPhone.setError("Phone Number is Required");
//            editTextPhone.requestFocus();
//            return;
//        }
//        if (phoneNumber.length() < 10) {
//            editTextPhone.setError("Enter a Valid Phone Number");
//            editTextPhone.requestFocus();
//            return;
//        }
//
//        pgsBar.setVisibility(View.VISIBLE);
//        findViewById(R.id.sendingCodeText).setVisibility(View.VISIBLE);
//        displayVerficationInput("hide");
//
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                phoneNumber,        // Phone number to verify
//                60,                 // Timeout duration
//                TimeUnit.SECONDS,   // Unit of timeout
//                this,               // Activity (for callback binding)
//                mCallbacks);        // OnVerificationStateChangedCallbacks
//
//    }
//
//    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//        @Override
//        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//            Log.i(TAG, "Complete phone verfication " + phoneAuthCredential);
//            findViewById(R.id.sendingCodeText).setVisibility(View.GONE);
//            pgsBar.setVisibility(View.GONE);
//            displayEnterVerficationInput("show");
//        }
//
//        @Override
//        public void onVerificationFailed(FirebaseException e) {
//            displayVerficationInput("show");
//            Log.i(TAG, "Failed phone verfication " + e);
//            findViewById(R.id.sendingCodeText).setVisibility(View.GONE);
//            pgsBar.setVisibility(View.GONE);
//            displayVerficationInput("show");
//            showErrorToast( "Verification error:" + e.getMessage());
//        }
//
//        @Override
//        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//            super.onCodeSent(s, forceResendingToken);
//            codeSent = s;
//        }
//    };
//
//    public void onClickSendCode (View view){
//
//    }
//
//    private void showErrorToast(String message){
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//    }
//}
