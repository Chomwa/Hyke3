package com.eafricar.hyke;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkAppStatus {

    private static NetworkAppStatus instance = new NetworkAppStatus();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static NetworkAppStatus getInstance(Context ctx){
        context = ctx.getApplicationContext();
        return instance;
    }

    public Boolean isOnline(){
        try{
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context. CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        }catch (Exception e){

            System.out.println( "Check Connectivity Exception:" + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }
}
