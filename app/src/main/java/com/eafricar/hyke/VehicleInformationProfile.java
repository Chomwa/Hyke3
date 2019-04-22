package com.eafricar.hyke;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class VehicleInformationProfile extends AppCompatActivity {

    private EditText mCarMake, mCarModel, mCarYear,
            mCarDoors, mCarPlate, mCarColor;

    private Button  mConfirm;

    private ImageButton mBack;

    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase;

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
        setContentView(R.layout.activity_vehicle_information_profile);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID).child("VehicleInformation");

        mCarMake = (EditText) findViewById(R.id.car_make);
        mCarModel = (EditText) findViewById(R.id.car_model);
        mCarYear = (EditText) findViewById(R.id.car_year);
        mCarDoors = (EditText) findViewById(R.id.car_doors);
        mCarPlate = (EditText) findViewById(R.id.car_license_plate);
        mCarColor = (EditText) findViewById(R.id.car_color);

        mBack = (ImageButton) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.next);

        mConfirm.setOnClickListener(new View.OnClickListener() {
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

        getUserVehicleInformation();
    }

    private void getUserVehicleInformation() {

        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("Car Make")!=null){
                        mMake = map.get("Car Make").toString();
                        mCarMake.setText(mMake);
                    }
                    if(map.get("Car Model")!=null){
                        mModel = map.get("Car Model").toString();
                        mCarModel.setText(mModel);
                    }
                    if(map.get("Car Year")!=null){
                        mYear = map.get("Car Year").toString();
                        mCarYear.setText(mYear);

                    }
                    if(map.get("Car Plate")!=null) {
                        mPlate = map.get("Car Plate").toString();
                        mCarPlate.setText(mPlate);

                    }
                    if(map.get("Car Doors")!=null){
                        mDoors = map.get("Car Doors").toString();
                        mCarDoors.setText(mDoors);

                    }
                    if(map.get("Car color")!=null){
                        mColor = map.get("Car color").toString();
                        mCarColor.setText(mColor);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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

        mDriverDatabase.updateChildren(userInfo);

        finish();
    }
}
