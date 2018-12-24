package com.eafricar.hyke;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

//import dmax.dialog.SpotsDialog;

public class CustomerLoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private EditText mEmailField, mPassword;
    private Button mLogin, mCreateAccount, mPhoneRegistration;

    private TextView mText, mForgotPassword;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private LinearLayout mRegistration_Section;

    //Google Sign in Method variables
    private LinearLayout mProf_Section;
    private Button mNext, mSignOut;
    private SignInButton mSignIn;
    private EditText mFirstNameField, mLastNameField, mEmailProfSectionField, mPhoneField;
    private ImageView mProfilePic;
    private GoogleApiClient googleApiClient;

    private static final int REQ_CODE = 9001;
    private DatabaseReference mCustomerDatabase;

    private String userID;
    private String mName;
    private String mLastName;
    private String mEmail;
    private String mPhone;

    private Uri resultUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmailField = (EditText) findViewById(R.id.email);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin = (Button) findViewById(R.id.login);
        mCreateAccount = (Button) findViewById(R.id.create_account);

        mPhoneRegistration = (Button) findViewById(R.id.phonenumberregistration);

        mText = (TextView) findViewById(R.id.textview);
        mForgotPassword = (TextView) findViewById(R.id.text_forgot_password);

        //calling Dialog


        //create forgot password function
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create seperate Function
                showDialogForgotPwd();
            }
        });

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerLoginActivity.this, CustomerRegistration.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmailField.getText().toString();
                final String password = mPassword.getText().toString();
                if (email.isEmpty()){
                    mEmailField.setError("Email is Required");
                    mEmailField.requestFocus();
                    return;
                }if (password.isEmpty()){
                    mPassword.setError("Password is Required");
                    mPassword.requestFocus();
                } else{
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CustomerLoginActivity.this, "sign in error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                }

            }
        });

        mPhoneRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CustomerLoginActivity.this, PhoneNumberVerification.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mRegistration_Section = (LinearLayout) findViewById(R.id.registration_section);

        //Calling Google Sign in method Variables.
        mProf_Section = (LinearLayout) findViewById(R.id.prof_section);
        mNext = (Button) findViewById(R.id.next);
        mSignOut = (Button) findViewById(R.id.signout);
        mSignIn = (SignInButton) findViewById(R.id.googlesignin);
        mFirstNameField = (EditText) findViewById(R.id.name);
        mLastNameField = (EditText) findViewById(R.id.lastname);
        mEmailProfSectionField = (EditText) findViewById(R.id.email_profile_section);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mProfilePic = (ImageView) findViewById(R.id.imageview);

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });
        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = mFirstNameField.getText().toString();
                final String lastname = mLastNameField.getText().toString();
                final String phone = mPhoneField.getText().toString();
                final String email = mEmailProfSectionField.getText().toString();

                if (name.isEmpty()) {
                    mFirstNameField.setError("First Name is Required");
                    mFirstNameField.requestFocus();
                    return;
                }
                if (lastname.isEmpty()) {
                    mLastNameField.setError("Last Name is Required");
                    mLastNameField.requestFocus();
                    return;
                }
                if (email.isEmpty()) {
                    mEmailProfSectionField.setError("Email Address is Required");
                    mEmailProfSectionField.requestFocus();
                    return;
                }
                if (phone.isEmpty()) {
                    mPhoneField.setError("Phone Number is Required");
                    mPhoneField.requestFocus();
                    return;
                }else {
                    saveUserInformation();
                    Intent intent = new Intent(CustomerLoginActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });


        mProf_Section.setVisibility(View.GONE);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestId()
                .requestProfile()
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions)
                .build();
    }

    //forgot password function
    public android.app.AlertDialog waitingDialog;
    private void showDialogForgotPwd(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerLoginActivity.this);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        //call layout inflator edit text and buttons
        LayoutInflater inflater = LayoutInflater.from(CustomerLoginActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_password, null);

        final MaterialEditText edtEmail = (MaterialEditText) forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        //set Button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                waitingDialog = new SpotsDialog(CustomerLoginActivity.this, "Waiting");
               waitingDialog.show();

                //Reset Password
                mAuth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(findViewById(R.id.customer_login), "Reset Password Link Sent to Email", Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(findViewById(R.id.customer_login), ""+e.getMessage(), Snackbar.LENGTH_LONG)
                                .show();

                    }
                });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

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

    private void SignIn(){

        Intent intent = Auth.GoogleSignInApi.getSignInIntent (googleApiClient);
        startActivityForResult(intent, REQ_CODE);

    }
    private void SignOut(){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                UpdateUI(false);
            }
        });

    }
    private void handleResult(GoogleSignInResult result){

        if (result.isSuccess()){

            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getGivenName();
            String last_name = account.getFamilyName();
            String email = account.getEmail();
            String img_url = "";

            if (account.getPhotoUrl()== null){
                Toast.makeText(getApplicationContext(),
                        "Could not get Profile Image Set Profile Image", Toast.LENGTH_LONG).show();
            }else {
                img_url = account.getPhotoUrl().toString();
            }

            mFirstNameField.setText(name);
            mLastNameField.setText(last_name);
            mEmailProfSectionField.setText(email);

            if (img_url.isEmpty()){
                Toast.makeText(getApplicationContext(),
                        "Could not get Profile Image Set Profile Image", Toast.LENGTH_LONG).show();
            }
            else {
                Glide.with(this).load(img_url).into(mProfilePic);
            }
            firebaseAuthWithGoogle (account);

            UpdateUI(true);
        }
        else{
            UpdateUI(false);
        }

    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.customer_login), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void UpdateUI(boolean isLogin){

        if (isLogin){
            mProf_Section.setVisibility(View.VISIBLE);
            mSignIn.setVisibility(View.GONE);
            mEmailField.setVisibility(View.GONE);
            mPassword.setVisibility(View.GONE);
            mCreateAccount.setVisibility(View.GONE);
            mPhoneRegistration.setVisibility(View.GONE);
            mText.setVisibility(View.GONE);
            mRegistration_Section.setVisibility(View.GONE);
            mLogin.setVisibility(View.GONE);
            mForgotPassword.setVisibility(View.GONE);
        }
        else{
            mProf_Section.setVisibility(View.GONE);
            mSignIn.setVisibility(View.VISIBLE);
            mEmailField.setVisibility(View.VISIBLE);
            mPassword.setVisibility(View.VISIBLE);
            mCreateAccount.setVisibility(View.VISIBLE);
            mPhoneRegistration.setVisibility(View.VISIBLE);
            mText.setVisibility(View.VISIBLE);
            mRegistration_Section.setVisibility(View.VISIBLE);
            mLogin.setVisibility(View.VISIBLE);
            mForgotPassword.setVisibility(View.VISIBLE);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQ_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);

        }
    }

    // Save User Information to Database
    private void saveUserInformation() {
        userID = mAuth.getCurrentUser().getUid();

        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        mName = mFirstNameField.getText().toString();
        mLastName = mLastNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mEmail = mEmailProfSectionField.getText().toString();


        Map userInfo = new HashMap();
        userInfo.put("first name", mName);
        userInfo.put("last name", mLastName);
        userInfo.put("Email", mEmail);
        userInfo.put("phone", mPhone);
        mCustomerDatabase.updateChildren(userInfo);


        if (resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mCustomerDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.googlesignin:
                SignIn();
                break;
            case R.id.signout:
                SignOut();
                break;
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(CustomerLoginActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

    }
}
