package com.eafricar.hyke;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class VehicleInformation extends AppCompatActivity {

    private EditText mCarMake, mCarModel, mCarYear,
            mCarDoors, mCarPlate, mCarColor;

    private Button mBack, mNext;

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userID;
    private String mMake;
    private String mModel;
    private String mYear;
    private String mPlate;
    private String mDoors;
    private String mColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_information);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID).child("VehicleInformation");

        mCarMake = (EditText) findViewById(R.id.car_make);
        mCarModel = (EditText) findViewById(R.id.car_model);
        mCarYear = (EditText) findViewById(R.id.car_year);
        mCarDoors = (EditText) findViewById(R.id.car_doors);
        mCarPlate = (EditText) findViewById(R.id.car_license_plate);
        mCarColor = (EditText) findViewById(R.id.car_color);

        mBack = (Button) findViewById(R.id.back2);
        mNext = (Button) findViewById(R.id.next2);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String make = mCarMake.getText().toString();
                final String model = mCarModel.getText().toString();
                final String year = mCarYear.getText().toString();
                final String doors = mCarDoors.getText().toString();
                final String plate = mCarPlate.getText().toString();
                final String color = mCarColor.getText().toString();

                if (make.isEmpty()) {
                    mCarMake.setError("Car Make is Required");
                    mCarMake.requestFocus();
                    return;
                }
                if (model.isEmpty()) {
                    mCarModel.setError("Car Model is Required");
                    mCarModel.requestFocus();
                    return;
                }
                if (year.isEmpty()) {
                    mCarYear.setError("Car Year is Required");
                    mCarYear.requestFocus();
                    return;
                }
                if (doors.isEmpty()) {
                    mCarDoors.setError("Number of Doors is Required");
                    mCarDoors.requestFocus();
                    return;
                }
                if (plate.isEmpty()) {
                    mCarPlate.setError("Car License Plate is Required");
                    mCarPlate.requestFocus();
                    return;
                }
                if (color.isEmpty()) {
                    mCarColor.setError("Color of Car is Required");
                    mCarColor.requestFocus();
                    return;
                }else {
                    saveUserInformation();
                    Intent intent = new Intent(VehicleInformation.this, DriverMapActivity.class);
                    startActivity(intent);
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
    }

    private void saveUserInformation(){
        mMake = mCarMake.getText().toString();
        mModel = mCarModel.getText().toString();
        mYear = mCarYear.getText().toString();
        mPlate = mCarPlate.getText().toString();
        mDoors = mCarDoors.getText().toString();
        mColor = mCarColor.getText().toString();


        Map userInfo = new HashMap();
        userInfo.put("Car Make", mMake);
        userInfo.put("Car Model", mModel);
        userInfo.put("Car Year", mYear);
        userInfo.put("Car Plate", mPlate);
        userInfo.put("Car Doors", mDoors);
        userInfo.put("Car color", mColor);

        mCustomerDatabase.updateChildren(userInfo);

        finish();
    }
}
