package com.eafricar.hyke;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField,mLastNameField,mEmailField;

    private TextView mTxtFirstName,mTxtLastName, mTxtEmail, mTxtPhone;

    private Button  mConfirm, mEditButton,mCancel;

    private ImageButton mBack;

    private ImageView mProfileImage;

    private LinearLayout mEditSection, mTextSection;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userID;
    private String mName;
    private String mLastName;
    private String mEmail;
    private String mPhone;


    private String mProfileImageUrl;

    private Uri resultUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        mNameField = (EditText) findViewById(R.id.name);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mLastNameField = (EditText) findViewById(R.id.lastname);
        mEmailField = (EditText) findViewById(R.id.email);

        mTxtFirstName = (TextView) findViewById(R.id.txtname);
        mTxtLastName = (TextView) findViewById(R.id.txtlastname);
        mTxtEmail = (TextView) findViewById(R.id.txtemail);
        mTxtPhone = (TextView) findViewById(R.id.txtphone);

        mEditSection = (LinearLayout) findViewById(R.id.edit_section);
        mTextSection = (LinearLayout) findViewById(R.id.text_section);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mProfileImage.setEnabled(false);

        mBack = (ImageButton) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);
        mEditButton = (Button) findViewById(R.id.editbutton);
        mCancel = (Button) findViewById(R.id.done);


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);




        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEditSection.setVisibility(View.GONE);
                mTextSection.setVisibility(View.VISIBLE);
                mBack.setVisibility(View.VISIBLE);
                mProfileImage.setEnabled(false);
            }
        });

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextSection.setVisibility(View.GONE);
                mEditSection.setVisibility(View.VISIBLE);
                mProfileImage.setEnabled(true);
                mBack.setVisibility(View.GONE);
            }
        });

        getUserInfo();

    }
    private void getUserInfo(){
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("firstName")!=null){
                        mName = map.get("firstName").toString();
                        mNameField.setText(mName);
                        mTxtFirstName.setText(mName);
                    }
                    if(map.get("lastName")!=null){
                        mLastName = map.get("lastName").toString();
                        mLastNameField.setText(mLastName);
                        mTxtLastName.setText(mLastName);
                    }
                    if(map.get("email")!=null) {
                        mEmail = map.get("email").toString();
                        mEmailField.setText(mEmail);
                        mTxtEmail.setText(mEmail);
                    }
                    if(map.get("phoneNumber")!=null){
                        mPhone = map.get("phoneNumber").toString();
                        mPhoneField.setText(mPhone);
                        mTxtPhone.setText(mPhone);
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mLastName = mLastNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mEmail = mEmailField.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("firstName", mName);
        userInfo.put("lastName", mLastName);
        userInfo.put("email", mEmail);
        userInfo.put("phoneNumber", mPhone);
        mCustomerDatabase.updateChildren(userInfo);

        if(resultUri != null) {

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



            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUrl = task.getResult();

                        Map newImage = new HashMap();
                        newImage.put("profileImageUrl", downloadUrl.toString());
                        mCustomerDatabase.updateChildren(newImage);

                        finish();
                        return;
                    }
                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });



          /*  filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri downloadUrl) {


                    Map newImage = new HashMap();
                    newImage.put("driverLicenseImageUrl", downloadUrl.toString());
                    mCustomerDatabase.updateChildren(newImage);

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
            }); */
        }else{
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mProfileImage.setImageURI(resultUri);
        }
    }

    //creating change password dialog box
   /* private void showDialogChangePwd() {

        //call AlertDialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerSettingsActivity.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill in all Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //Call Button and function

        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(CustomerSettingsActivity.this);
                waitingDialog.show();

                if (edtNewPassword.getText().toString().equals(edtRepeatNewPassword.getText().toString()))
                {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    //gettin auth credentials from the user for re-authentication

                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatNewPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            //Update password information Column
                                                            Map<String,Object> password = new HashMap<>();

                                                            password.put("password", edtRepeatNewPassword.getText().toString());

                                                            DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                                                            customerReference.updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                                Toast.makeText(CustomerSettingsActivity.this, "Password was changed Successfully", Toast.LENGTH_SHORT).show();
                                                                            else
                                                                                Toast.makeText(CustomerSettingsActivity.this, "Password was changed but not updated to Customer Information", Toast.LENGTH_SHORT).show();

                                                                            waitingDialog.dismiss();

                                                                        }
                                                                    });



                                                        }else {
                                                            Toast.makeText(CustomerSettingsActivity.this, "Password could not be changed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(CustomerSettingsActivity.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }else{
                    waitingDialog.dismiss();
                    Toast.makeText(CustomerSettingsActivity.this, "Password does not Match", Toast.LENGTH_SHORT).show();
                }


            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //remove dialogue
                dialogInterface.dismiss();
            }
        });

        //show dialog
        alertDialog.show();
    } */
}
