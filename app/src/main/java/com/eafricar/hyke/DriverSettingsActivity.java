package com.eafricar.hyke;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

public class DriverSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mLastNameField, mPhoneField, mCarField,mEmailField;

    private TextView mTxtFirstName,mTxtLastName, mTxtEmail,
            mTxtPhone, mTxtCar, mTxtService;

    private TextView mDriversLicense, mVehicleInformation;

    private Button mConfirm, mEditButton,mCancel;

    private ImageButton mBack;

    private LinearLayout mEditSection, mTextSection;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;

    private String userID;
    private String mName;
    private String mLastName;
    private String mPhone;
    private String mCar;
    private String mService;
    private String mEmail;
    private String mProfileImageUrl;

    private Uri resultUri;

    private RadioGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);


        mNameField = (EditText) findViewById(R.id.name);
        mLastNameField = (EditText) findViewById(R.id.lastname);
        mEmailField = (EditText) findViewById(R.id.email);
        mPhoneField = (EditText) findViewById(R.id.phone);
        mCarField = (EditText) findViewById(R.id.car);

        mTxtFirstName = (TextView) findViewById(R.id.txtname);
        mTxtLastName = (TextView) findViewById(R.id.txtlastname);
        mTxtEmail = (TextView) findViewById(R.id.txtemail);
        mTxtPhone = (TextView) findViewById(R.id.txtphone);
        mTxtCar = (TextView) findViewById(R.id.txtcar);
        mTxtService = (TextView) findViewById(R.id.txtservice);

        mDriversLicense = (TextView) findViewById(R.id.driver_license);
        mVehicleInformation = (TextView) findViewById(R.id.vehicle_information);

        mEditSection = (LinearLayout) findViewById(R.id.edit_section);
        mTextSection = (LinearLayout) findViewById(R.id.text_section);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);
        mProfileImage.setEnabled(false);

        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.HykeTaxi);

        mBack = (ImageButton) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);
        mEditButton = (Button) findViewById(R.id.editbutton);
        mCancel = (Button) findViewById(R.id.done);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);



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

        mDriversLicense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent Intent = new Intent(DriverSettingsActivity.this, DriverLicenseProfile.class); //add activities
                startActivity(Intent);

            }
        });

        mVehicleInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Intent = new Intent(DriverSettingsActivity.this, VehicleInformationProfile.class);
                startActivity(Intent);
            }
        });

        getUserInfo();

    }
    private void getUserInfo(){
        mDriverDatabase.addValueEventListener(new ValueEventListener() {
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
                    if(map.get("phoneNumber")!=null){
                        mPhone = map.get("phoneNumber").toString();
                        mPhoneField.setText(mPhone);
                        mTxtPhone.setText(mPhone);
                    }
                    if(map.get("email")!=null) {
                        mEmail = map.get("email").toString();
                        mEmailField.setText(mEmail);
                        mTxtEmail.setText(mEmail);
                    }
                    if(map.get("car")!=null){
                        mCar = map.get("car").toString();
                        mCarField.setText(mCar);
                        mTxtCar.setText(mCar);
                    }
                    if(map.get("service")!=null){
                        mService = map.get("service").toString();
                        switch (mService){
                            case"HyKeShared":
                                mRadioGroup.check(R.id.HykeShared);
                                mTxtService.setText(mService);
                                break;
                            case"HyKePersonal":
                                mRadioGroup.check(R.id.HykePersonal);
                                mTxtService.setText(mService);
                                break;
                            case"HyKeTaxi":
                                mRadioGroup.check(R.id.HykeTaxi);
                                mTxtService.setText(mService);
                                break;
                        }
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
        mCar = mCarField.getText().toString();
        mEmail = mEmailField.getText().toString();

        int selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectId);

        if (radioButton.getText() == null){
            return;
        }

        mService = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("firstName", mName);
        userInfo.put("lastName", mLastName);
        userInfo.put("phoneNumber", mPhone);
        userInfo.put("email", mEmail);
        userInfo.put("car", mCar);
        userInfo.put("service", mService);
        mDriverDatabase.updateChildren(userInfo);

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

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

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
                        mDriverDatabase.updateChildren(newImage);

                        finish();
                        return;
                    }
                }
            });

         /*   uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("profileImageUrl", downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);

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
}
