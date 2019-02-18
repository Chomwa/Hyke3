package com.eafricar.hyke;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eafricar.hyke.Model.User;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriverRegistration extends AppCompatActivity {


    private EditText mEmailField, mPassword, confirmPassword, userPhoneNumber, firstName, lastName;
    private List<String> steps = new ArrayList<>(Arrays.asList("Email", "Phone number", "Password", "Bio"));
    private List<Integer> enterEmailIds, phoneNumberIds, passwordIds, bioIds;
    private String currentStep = "Email";
    private static final String TAG = "Customer registration";
    public static final int REQUEST_CODE = 10;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDriverDatabase;
    private DatabaseReference mUsers;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    Activity activity = DriverRegistration.this;
    String wantPermission = android.Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private User user;

    private ProgressBar pgsBar;

    private Button mNext;
    private TextView mPleaseWait;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);

        pgsBar = findViewById(R.id.pBar);

        mNext = (Button) findViewById(R.id.next);

        mPleaseWait = (TextView) findViewById(R.id.pleaseWait);

        mAuth = FirebaseAuth.getInstance();
        mDriverDatabase = FirebaseDatabase.getInstance();
        mUsers = mDriverDatabase.getReference("Users");

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(DriverRegistration.this, DriverLicenseDetails.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        enterEmailIds = addViewIds(R.id.enterYourEmail, R.id.email, R.id.weSendReceipts);
        phoneNumberIds = addViewIds(R.id.phone_number, R.id.whatsYourNumber, R.id.phone);
        passwordIds = addViewIds(R.id.enterYourPassword, R.id.password, R.id.confirm_password);
        bioIds = addViewIds(R.id.first_name, R.id.last_name, R.id.whatAreYourFullnames);
        mEmailField = findViewById(R.id.email);
        user = new User();
        getUserEmail();

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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        finish();
    }

    private void displayRegistrationFields(String visibility, List<Integer> viewIds) {
        int display = visibility.equals("Show") ? View.VISIBLE : View.GONE;
        for (Integer viewId : viewIds) {
            findViewById(viewId).setVisibility(display);
        }
    }

    public void onClickRegNext(View v) {
        switch (currentStep) {
            case "Email":
                String email =  mEmailField.getText().toString();
                if (TextUtils.isEmpty(email)){
                    getUserEmail();
                } else{
                    user.setEmail(email);
                    nextStep();
                }
                break;

            case "Phone number":
                userPhoneNumber = findViewById(R.id.phone);
                autoSetUserPhoneNumber();
                if (isValidPhoneNumber()) {
                    userPhoneNumber.setText(getSanitizedPhoneNumber());
                    user.setPhoneNumber(userPhoneNumber.getText().toString());
                    nextStep();
                }
                break;

            case "Password":
                mPassword = findViewById(R.id.password);
                confirmPassword = findViewById(R.id.confirm_password);
                if (isValidPassword()) {
                    user.setPassword(mPassword.getText().toString());
                    nextStep();
                }
                break;

            case "Bio":
                firstName = findViewById(R.id.first_name);
                lastName = findViewById( R.id.last_name);
                if (!(isFieldEmpty(firstName) && isFieldEmpty(lastName))){
                    user.setFirstName(firstName.getText().toString());
                    user.setLastName(lastName.getText().toString());
                    pgsBar.setVisibility(View.VISIBLE);
                    displayRegistrationFields("Hide", bioIds);
                    displayRegistrationFields("Hide", enterEmailIds);
                    displayRegistrationFields("Hide", phoneNumberIds);
                    displayRegistrationFields("Hide", passwordIds);
                    mNext.setVisibility(View.GONE);
                    mPleaseWait.setVisibility(View.VISIBLE);
                    registerUser();
                }
                break;

        }
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            mEmailField.setText(accountName);
        }
    }

    private List<Integer> addViewIds(int firstId, int secondId, int lastId) {
        List<Integer> ids = new ArrayList<>();
        ids.add(firstId);
        ids.add(secondId);
        ids.add(lastId);
        return ids;
    }

    private void getUserEmail() {
        displayRegistrationFields("Show", enterEmailIds);
        Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, null, null,
                null, null);
        startActivityForResult(googlePicker, REQUEST_CODE);
    }


    private void nextStep() {
        currentStep = steps.get(steps.indexOf(currentStep) + 1);
        Log.i(TAG, "Loading registration step " + currentStep);
        switch (currentStep) {
            case "Email":
                displayRegistrationFields("Show", enterEmailIds);
                break;

            case "Phone number":
                displayRegistrationFields("Hide", enterEmailIds);
                displayRegistrationFields("Show", phoneNumberIds);
                break;

            case "Password":
                displayRegistrationFields("Hide", phoneNumberIds);
                displayRegistrationFields("Show", passwordIds);
                break;
            case "Bio":
                displayRegistrationFields("Hide", passwordIds);
                displayRegistrationFields("Show", bioIds);
                break;
        }
    }

    private String getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(wantPermission);
            return "";
        }

        return phoneMgr.getLine1Number();
    }

    private void requestPermission(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
            Toast.makeText(activity, "Phone state permission allows us to get phone number. " +
                    "Please allow it for additional functionality.", Toast.LENGTH_LONG).show();

        ActivityCompat.requestPermissions(activity, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Phone number:  " + getPhone());
                } else {
                    Toast.makeText(activity, "Permission Denied. We can't get phone number.",
                            Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void autoSetUserPhoneNumber() {
        String phoneNumber = getPhone();
        if (!TextUtils.isEmpty(phoneNumber))userPhoneNumber.setText(phoneNumber);
    }


    private boolean isValidPhoneNumber() {
        boolean isValidNumber = true;
        String number = "+" + userPhoneNumber.getText().toString();
        Log.i(TAG, "Number received" + number);
        if (TextUtils.isEmpty(number))
            isValidNumber = showEditTextError("Phone Number is Required", userPhoneNumber);

        if (number.length() < 10)
            isValidNumber = showEditTextError("Enter a Valid Phone Number", userPhoneNumber);

        return isValidNumber;
    }

    private boolean showEditTextError(String error, EditText editText) {
        editText.setError(error);
        editText.requestFocus();
        return false;
    }

    private String getSanitizedPhoneNumber() {
        String number = userPhoneNumber.getText().toString();
        if (number.length() == 10)
            return "+26" + number;

        String numberPrefix = number.substring(0, 3);
        if (numberPrefix.equals("+26"))
            return number;

        if (numberPrefix.equals("260"))
            return "+" + number;

        return number;
    }

    private boolean isValidPassword() {
        boolean isValidNumber = true;
        String userPassword = mPassword.getText().toString();

        if (userPassword.isEmpty())
            isValidNumber = showEditTextError("Password is Required", mPassword);

        if (userPassword.length() < 6)
            isValidNumber = showEditTextError("Password should not be less than 6 characters",
                    mPassword);

        if (!userPassword.equals(confirmPassword.getText().toString()))
            isValidNumber = showEditTextError("Password should match", confirmPassword);

        return isValidNumber;

    }

    private boolean isFieldEmpty(EditText value){
        return TextUtils.isEmpty(value.getText().toString());
    }



    private void registerUser(){
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        mUsers.child("Drivers")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(DriverRegistration.this,
                                                "Registration Successful!", Toast.LENGTH_SHORT)
                                                .show();
                                      /*  Intent intent = new Intent(DriverRegistration.
                                                this, DriverRegistration.class);
                                        startActivity(intent);
                                        finish();*/
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(DriverRegistration.this,
                                                "Failed: " + e.getMessage(), Toast.LENGTH_SHORT)
                                                .show();

                                        //update Interface
                                        pgsBar.setVisibility(View.GONE);
                                        displayRegistrationFields("Show", bioIds);
                                        displayRegistrationFields("Hide", enterEmailIds);
                                        displayRegistrationFields("Hide", phoneNumberIds);
                                        displayRegistrationFields("Hide", passwordIds);
                                        mNext.setVisibility(View.VISIBLE);
                                        mPleaseWait.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DriverRegistration.this,
                                e.getMessage(), Toast.LENGTH_SHORT)
                                .show();

                        //update Interface
                        pgsBar.setVisibility(View.GONE);
                        displayRegistrationFields("Show", bioIds);
                        displayRegistrationFields("Hide", enterEmailIds);
                        displayRegistrationFields("Hide", phoneNumberIds);
                        displayRegistrationFields("Hide", passwordIds);
                        mNext.setVisibility(View.VISIBLE);
                        mPleaseWait.setVisibility(View.GONE);

                    }
                });

    }
}
