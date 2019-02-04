package com.eafricar.hyke.Common;

import com.google.firebase.database.FirebaseDatabase;

import java.security.PublicKey;

public class FirebaseSanitizer {

    FirebaseDatabase database;

    public FirebaseSanitizer(){
        database = FirebaseDatabase.getInstance();
    }

    public void removeCustomerRequest (String driverID){
        if (driverID != null){
            database.getReference().child("Users")
                    .child("Drivers").child(driverID).child("customerRequest").removeValue();
        }
    }

}
