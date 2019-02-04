package com.eafricar.hyke;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eafricar.hyke.Common.Common;
import com.eafricar.hyke.Model.FCMResponse;
import com.eafricar.hyke.Model.Notification;
import com.eafricar.hyke.Model.Sender;
import com.eafricar.hyke.Model.Token;
import com.eafricar.hyke.Remote.IFCMservice;
import com.eafricar.hyke.Remote.IGoogleAPI;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eafricar.hyke.Common.Common.mLastLocation;

public class CustomerCall extends AppCompatActivity {

    private TextView mTime, mDistance, mAddress;

    private Button mCancelRequest, mAcceptRequest;

    private MediaPlayer mediaPlayer;

    private IGoogleAPI mService;
    private IFCMservice mFCMService;

    private String customerId;

    private static final String TAG = "Call activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        mTime= (TextView) findViewById(R.id.txtTime);
        mDistance= (TextView) findViewById(R.id.txtDistance);
        mAddress= (TextView) findViewById(R.id.txtAddress);

        mAcceptRequest = (Button) findViewById(R.id.acceptRequest);
        mCancelRequest = (Button) findViewById(R.id.cancelRequest);

        mediaPlayer = MediaPlayer.create(this, R.raw.slow_spring_board);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if (getIntent() != null)
        {
            double lat = getIntent().getDoubleExtra("lat", -1.0);
            double lng = getIntent().getDoubleExtra("lng", -1.0);

            customerId = getIntent().getStringExtra("customer");
            getDirection(lat, lng);
            Log.i("TAG", customerId);
        }
        Log.i("TAG", customerId);


        //Request event
        mCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (!TextUtils.isEmpty(customerId))
                {
                    cancelBooking(customerId);
                }

            }
        });

        mAcceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!TextUtils.isEmpty(customerId))
                {
                    Log.i("TAG", customerId);
                    acceptBooking(customerId);
                }

            }
        });
        Query mDriverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        mDriverRatingDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    Log.i(TAG, zoneSnapshot.child("customerRequest").child("status").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void acceptBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Notice!", "Driver has Accepted your Request");
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                        if (response.body().success==1)
                        {
                            Toast.makeText(CustomerCall.this, "Request Accepted", Toast.LENGTH_SHORT)
                                    .show();

                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });

    }


    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

        Notification notification = new Notification("Notice!", "Driver has declined your Request");
        Sender sender = new Sender(token.getToken(), notification);

        mFCMService.sendMessage(sender)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                        if (response.body().success==1)
                        {
                            Toast.makeText(CustomerCall.this, "Request Declined", Toast.LENGTH_SHORT)
                                    .show();

                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void getDirection(double lat, double lng) {


        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ mLastLocation.getLatitude()+","+ mLastLocation.getLongitude()+"&"+
                    "destination="+ lat + ","+ lng +"&"+
                    "key=" +getResources().getString(R.string.google_direction_api);
            Log.d("HyKe",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());


                                JSONArray routes = jsonObject.getJSONArray("routes");

                                JSONObject object = routes.getJSONObject(0);

                                //get array called legs
                                JSONArray legs = object.getJSONArray("legs");

                                //get first element
                                JSONObject legsObject = legs.getJSONObject(0);

                                //get Distance
                                JSONObject distance = legsObject.getJSONObject("distance");
                                mDistance.setText(distance.getString("text"));

                                //get Time
                                JSONObject time = legsObject.getJSONObject("duration");
                                mTime.setText(time.getString("text"));

                                //get Address
                                String address = legsObject.getString("end_address");
                                mAddress.setText(address);

                            }catch (JSONException e)
                            {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                            Toast.makeText(CustomerCall.this,""+ t.getMessage(),Toast.LENGTH_SHORT)
                                    .show();

                        }
                    });
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {

        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {

        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }
}
