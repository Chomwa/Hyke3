package com.eafricar.hyke.Service;

import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.eafricar.hyke.CustomerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;

import java.util.logging.Handler;

public class MyFirebaseMessaging extends FirebaseMessagingService{

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if (remoteMessage.getNotification().getTitle().equals("Notice!")){

                android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MyFirebaseMessaging.this, "" + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });


        }else{

            Log.d("HyKe",remoteMessage.getNotification().getBody());
            //we are sending a firebase message that contains the customers Latlng, so we convert message to Latlng
            try {

                LatLng customer_location = new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

                Intent intent = new Intent(getBaseContext(), CustomerCall.class);
                intent.putExtra("lat", customer_location.latitude);
                intent.putExtra("lng", customer_location.longitude);
                intent.putExtra("customer", remoteMessage.getNotification().getTitle());

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);

            }catch (IllegalStateException | JsonSyntaxException exception)
            {

            }

        }


    }

}
