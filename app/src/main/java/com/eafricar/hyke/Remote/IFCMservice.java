package com.eafricar.hyke.Remote;

import com.eafricar.hyke.Model.FCMResponse;
import com.eafricar.hyke.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMservice {
    @Headers({
           "Content-Type:application/json",
            "Authorization:key=AAAAqG4eq94:APA91bFD-lFLcrQz2Hh-hT6D3PDc1XEcP2obfXipt2-CksvCPLQeIExvIiQ8k96EqDytSKmmaa7QSBGzRgo_5SrssNg0BZtkUoxZ4Dwv-eqZAJXMQrfQ3WG7o9xIJr57Hhw7OZUJux8t0m8VX3vapVFfo8smcaQc0w"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
