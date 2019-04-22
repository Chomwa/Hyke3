package com.eafricar.hyke;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class DriverLicenseProfile extends AppCompatActivity {

    private ImageView mDriverLicenseImage;

    private EditText mDriversFirstName, mDriversMiddleName, mDriversLastName,
            mDriversLicenseNumber,mDriversLicenseNumber2,mDriversLicenseNumber3,
            mDriverIdNumber,mDriverIdNumber2,mDriverIdNumber3, mDriverGender,
            mDriverDateOfBirth, mDriverPlaceofIssue, mDriverExpiryDate;

    private Button  mConfirm;

    private ImageButton mBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;

    private String userID;
    private String mName;
    private String mLastName;
    private String mMiddleName;
    private String mLicense;
    private String mLicense2;
    private String mLicense3;
    private String mNRC;
    private String mNRC2;
    private String mNRC3;
    private String mGender;
    private String mDOB;
    private String mPOI;
    private String mED;

    private String mProfileImageUrl;

    private Uri resultUri;

    private Calendar c;
    private DatePickerDialog.OnDateSetListener date;
    private DatePickerDialog.OnDateSetListener date2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_license_profile);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID).child("DriverLicenseDetails");

        mDriverLicenseImage = (ImageView) findViewById(R.id.drivers_license_image);

        mDriversFirstName = (EditText) findViewById(R.id.driver_license_first_name);
        mDriversMiddleName = (EditText) findViewById(R.id.driver_license_middlename);
        mDriversLastName = (EditText) findViewById(R.id.driver_license_lastname);
        mDriversLicenseNumber = (EditText) findViewById(R.id.driver_license_number);
        mDriversLicenseNumber2 = (EditText) findViewById(R.id.driver_license_number2);
        mDriversLicenseNumber3 = (EditText) findViewById(R.id.driver_license_number3);
        mDriverIdNumber = (EditText) findViewById(R.id.driver_id_number);
        mDriverIdNumber2 = (EditText) findViewById(R.id.driver_id_number2);
        mDriverIdNumber3 = (EditText) findViewById(R.id.driver_id_number3);
        mDriverGender = (EditText) findViewById(R.id.driver_gender);
        mDriverDateOfBirth = (EditText) findViewById(R.id.driver_birth_day);
        mDriverPlaceofIssue = (EditText) findViewById(R.id.license_place_of_issue);
        mDriverExpiryDate = (EditText) findViewById(R.id.license_expiry_date);

        mBack = (ImageButton) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.next_page);

        mDriverLicenseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        //calling Edit Text Date Variables
        c = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        date2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, monthOfYear);
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel2();
            }
        };


        // Calling birth Date dialog in Edit Text
        mDriverDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new DatePickerDialog(DriverLicenseProfile.this, date,
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        // Calling Expiry date dialog in Edit text
        mDriverExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(DriverLicenseProfile.this, date2,
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = mDriversFirstName.getText().toString();
                final String lastname = mDriversLastName.getText().toString();
                final String license = mDriversLicenseNumber.getText().toString();
                final String nrc = mDriverIdNumber.getText().toString();
                final String gender = mDriverGender.getText().toString();
                final String dob = mDriverDateOfBirth.getText().toString();
                final String poi = mDriverPlaceofIssue.getText().toString();
                final String ed = mDriverExpiryDate.getText().toString();

                if (name.isEmpty()) {
                    mDriversFirstName.setError("First Name is Required");
                    mDriversFirstName.requestFocus();
                    return;
                }
                if (lastname.isEmpty()) {
                    mDriversLastName.setError("Last Name is Required");
                    mDriversLastName.requestFocus();
                    return;
                }
                if (license.isEmpty()) {
                    mDriversLicenseNumber.setError("License Number is Required");
                    mDriversLicenseNumber.requestFocus();
                    return;
                }
                if (nrc.isEmpty()) {
                    mDriverIdNumber.setError("NRC/ID is Required");
                    mDriverIdNumber.requestFocus();
                    return;
                }
                if (gender.isEmpty()) {
                    mDriverGender.setError("Gender is Required");
                    mDriverGender.requestFocus();
                    return;
                }
                if (dob.isEmpty()) {
                    mDriverDateOfBirth.setError("Date of Birth is Required");
                    mDriverDateOfBirth.requestFocus();
                    return;
                }
                if (poi.isEmpty()) {
                    mDriverPlaceofIssue.setError("Place of Issue is Required");
                    mDriverPlaceofIssue.requestFocus();
                    return;
                }
                if (ed.isEmpty()) {
                    mDriverExpiryDate.setError("Expiry Date is Required");
                    mDriverExpiryDate.requestFocus();
                    return;
                } else {
                    saveUserInformation();
                    finish();
                    return;
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

        getUserInfo();
    }



    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDriverDateOfBirth.setText(sdf.format(c.getTime()));
    }

    private void updateLabel2() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mDriverExpiryDate.setText(sdf.format(c.getTime()));
    }

    private void getUserInfo() {

        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("first name")!=null){
                        mName = map.get("first name").toString();
                        mDriversFirstName.setText(mName);

                    }
                    if(map.get("last name")!=null){
                        mLastName = map.get("last name").toString();
                        mDriversLastName.setText(mLastName);

                    }
                    if(map.get("middle name")!=null){
                        mMiddleName = map.get("middle name").toString();
                        mDriversMiddleName.setText(mMiddleName);

                    }
                    if(map.get("license")!=null) {
                        mLicense = map.get("license").toString();
                        mDriversLicenseNumber.setText(mLicense);

                    }
                    if(map.get("license2")!=null) {
                        mLicense2 = map.get("license2").toString();
                        mDriversLicenseNumber2.setText(mLicense2);

                    }
                    if(map.get("license3")!=null) {
                        mLicense3 = map.get("license3").toString();
                        mDriversLicenseNumber3.setText(mLicense3);

                    }
                    if(map.get("id Number")!=null){
                        mNRC = map.get("id Number").toString();
                        mDriverIdNumber.setText(mNRC);

                    }
                    if(map.get("id Number2")!=null){
                        mNRC2 = map.get("id Number2").toString();
                        mDriverIdNumber2.setText(mNRC2);

                    }
                    if(map.get("id Number3")!=null){
                        mNRC3 = map.get("id Number3").toString();
                        mDriverIdNumber3.setText(mNRC3);

                    }
                    if(map.get("Date of Birth")!=null){
                        mDOB = map.get("Date of Birth").toString();
                        mDriverDateOfBirth.setText(mDOB);
                    }
                    if(map.get("Gender")!=null){
                        mGender = map.get("Gender").toString();
                        mDriverGender.setText(mGender);
                    }
                    if(map.get("Place of Issue")!=null){
                        mPOI = map.get("Place of Issue").toString();
                        mDriverPlaceofIssue.setText(mPOI);
                    }
                    if(map.get("Expiry Date")!=null){
                        mED = map.get("Expiry Date").toString();
                        mDriverExpiryDate.setText(mED);
                    }
                    if(map.get("driverLicenseImageUrl")!=null){
                        mProfileImageUrl = map.get("driverLicenseImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).into(mDriverLicenseImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveUserInformation() {
        mName = mDriversFirstName.getText().toString();
        mLastName = mDriversLastName.getText().toString();
        mMiddleName = mDriversMiddleName.getText().toString();
        mLicense = mDriversLicenseNumber.getText().toString();
        mLicense2 = mDriversLicenseNumber2.getText().toString();
        mLicense3 = mDriversLicenseNumber3.getText().toString();
        mNRC = mDriverIdNumber.getText().toString();
        mNRC2 = mDriverIdNumber2.getText().toString();
        mNRC3 = mDriverIdNumber3.getText().toString();
        mDOB = mDriverDateOfBirth.getText().toString();
        mGender = mDriverGender.getText().toString();
        mPOI = mDriverPlaceofIssue.getText().toString();
        mED = mDriverExpiryDate.getText().toString();


        Map userInfo = new HashMap();
        userInfo.put("first name", mName);
        userInfo.put("last name", mLastName);
        userInfo.put("middle name", mMiddleName);
        userInfo.put("license", mLicense);
        userInfo.put("license2", mLicense2);
        userInfo.put("license3", mLicense3);
        userInfo.put("id Number", mNRC);
        userInfo.put("id Number2", mNRC2);
        userInfo.put("id Number3", mNRC3);
        userInfo.put("Date of Birth", mDOB);
        userInfo.put("Gender", mGender);
        userInfo.put("Place of Issue", mPOI);
        userInfo.put("Expiry Date", mED);
        mDriverDatabase.updateChildren(userInfo);


        if (resultUri != null) {

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("driver_license_images").child(userID).child("DriverLicenseDetails");
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
                        newImage.put("driverLicenseImageUrl", downloadUrl.toString());
                        mDriverDatabase.updateChildren(newImage);

                        finish();
                        return;
                    }
                }
            });


        /*    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  //  Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Uri downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("driverLicenseImageUrl", downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            }); */
        } else {
            finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            mDriverLicenseImage.setImageURI(resultUri);
        }
    }
}
