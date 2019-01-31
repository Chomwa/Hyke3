package com.eafricar.hyke.Common;

import android.location.Location;

import com.eafricar.hyke.Remote.FCMClient;
import com.eafricar.hyke.Remote.IFCMservice;
import com.eafricar.hyke.Remote.IGoogleAPI;
import com.eafricar.hyke.Remote.RetrofitClient;

public class Common {

    public static String currentToken = "";

    public static final String token_table = "Tokens";

    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static final String baseURL = "https://maps.googleapis.com/";

    public static Location mLastLocation = null;



    public static IFCMservice getFCMService(){

        return FCMClient.getClient(fcmUrl).create(IFCMservice.class);
    }

    public static IGoogleAPI getGoogleAPI(){

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
