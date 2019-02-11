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
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class DriverLicenseDetails extends AppCompatActivity {

    private ImageView mDriverLicenseImage;

    private EditText mDriversFirstName, mDriversMiddleName, mDriversLastName,
            mDriversLicenseNumber,mDriversLicenseNumber2,mDriversLicenseNumber3,
            mDriverIdNumber,mDriverIdNumber2,mDriverIdNumber3, mDriverGender,
            mDriverDateOfBirth, mDriverPlaceofIssue, mDriverExpiryDate;

    private Button mBack, mNext;

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
        setContentView(R.layout.activity_driver_license_details);

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

        mBack = (Button) findViewById(R.id.back);
        mNext = (Button) findViewById(R.id.next_page);

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

                new DatePickerDialog(DriverLicenseDetails.this, date,
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
                new DatePickerDialog(DriverLicenseDetails.this, date2,
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
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
                    Intent intent = new Intent(DriverLicenseDetails.this, VehicleInformation.class);
                    startActivity(intent);
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
        userInfo.put("id Number", mNRC2);
        userInfo.put("id Number", mNRC3);
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
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    Map newImage = new HashMap();
                    newImage.put("driverLicenseImageUrl", downloadUrl.toString());
                    mDriverDatabase.updateChildren(newImage);

                    finish();
                    return;
                }
            });
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
