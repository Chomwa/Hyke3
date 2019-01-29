package com.eafricar.hyke;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.widget.Toolbar;

import com.arsy.maps_library.MapRipple;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.eafricar.hyke.Common.Common;
import com.eafricar.hyke.Model.FCMResponse;
import com.eafricar.hyke.Model.Notification;
import com.eafricar.hyke.Model.Sender;
import com.eafricar.hyke.Model.Token;
import com.eafricar.hyke.Remote.IFCMservice;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import static com.eafricar.hyke.Common.Common.mLastLocation;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener,RoutingListener {

    private GoogleMap mMap;

    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;

    private Button  mRequest, mSetDestination;

    private LatLng pickupLocation;

    private Boolean requestBol = false;

    private Marker pickupMarker, destinationMarker;

    private SupportMapFragment mapFragment;

    private String destination, requestService, mPickUpName;

    private LatLng destinationLatLng, pickupLatLng;

    private LinearLayout mDriverInfo, mRideRequestSection, mSetDestinationSection;

    private ImageView mDriverProfileImage, NavigationHeaderImage, mCancel, mEditInfo;

    private TextView mDriverName, mDriverPhone, mDriverCar,
            mNavigationHeaderTextFirstName,mNavigationHeaderTextLastName,
            mNavigationHeaderTextPhoneNumber, mPickUpText, mDestinationText,
            mSetDestinationName;

    private RadioGroup mRadioGroup;

    private RatingBar mRatingBar;

    private DatabaseReference mCustomerDatabase;
    private String userID;

    private String mNavigationHeaderImageUrl;
    //tool bar variables
    private Toolbar mToolbar;
    private ImageView mToggleImage;
    private DrawerLayout mDrawer;

    //place auto complete fragment
    private CardView mSetLocation, mLocationInfoSection;
    private PlaceAutocompleteFragment mPlace_Location, mPlace_Destination;
    private AutocompleteFilter mTypeFilter;


    //Map Ripple Animation
    private MapRipple mapRipple;

    //vehicle Type
    private ImageView mShared, mTaxi,mPersonal;

    //Send Notification
    private IFCMservice mService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_customer_map);

        mService = Common.getFCMService();


        //Calling tool bar and tool bar functions
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //remove tool bar title

        mToggleImage = (ImageView) findViewById(R.id.toolbarprofileImage); //calling tool bar toggle image
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);

        //calling toolbar toggle in image view
        mToggleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawer.isDrawerVisible(GravityCompat.START)) {
                    mDrawer.closeDrawer(GravityCompat.START);
                } else {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            }
        });



        //Calling Navigation Drawer View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);// listen for navigation view items
        //calling Navigation Header
        View header = navigationView.getHeaderView(0);
        NavigationHeaderImage = (ImageView) header.findViewById(R.id.profileImage);//navigation profile picture
        mNavigationHeaderTextFirstName = (TextView) header.findViewById(R.id.navigationtextFirstName); //navigation user first name
        mNavigationHeaderTextLastName = (TextView) header.findViewById(R.id.navigationtextLastName); //navigation user last name
        mNavigationHeaderTextPhoneNumber = (TextView) header.findViewById(R.id.navigationtextPhoneNumber); //navigation user phone number

        //Database reference in relation to navigation header image and tool bar toggle image
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);

        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                if(map.get("profileImageUrl")!=null){
                    mNavigationHeaderImageUrl = map.get("profileImageUrl").toString();
                    Glide.with(getApplication()).load(mNavigationHeaderImageUrl).into(NavigationHeaderImage);// putting profile picture in header image view
                }
                    if(map.get("profileImageUrl")!=null){
                        mNavigationHeaderImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mNavigationHeaderImageUrl).into(mToggleImage); // putting profile picture in tool bar toggle image
                    }
              }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
         //Database reference with reference to Navigation header Text
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("first name")!=null){
                        mNavigationHeaderTextFirstName.setText(dataSnapshot.child("first name").getValue().toString()); //navigation user first name
                    }
                    if(dataSnapshot.child("first name")!=null){
                        mSetDestinationName.setText(dataSnapshot.child("first name").getValue().toString() + ","); //navigation user first name
                    }
                    if(dataSnapshot.child("last name")!=null){
                        mNavigationHeaderTextLastName.setText(dataSnapshot.child("last name").getValue().toString()); //navigation user last name
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mNavigationHeaderTextPhoneNumber.setText(dataSnapshot.child("phone").getValue().toString()); //navigation user phone number
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Calling Location services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        destinationLatLng = new LatLng(0.0,0.0);
        pickupLatLng = new LatLng(0.0,0.0);

        //Calling Driver Information Variables when receiving a customer ride request
        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo); // Driver info Linear layout which is currently hidden

        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);

        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mDriverCar = (TextView) findViewById(R.id.driverCar);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        //mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        //mRadioGroup.check(R.id.HykeShared);


        mRideRequestSection = (LinearLayout) findViewById(R.id.ride_request_section);
        mRequest = (Button) findViewById(R.id.request);
        mSetDestination = (Button) findViewById(R.id.set_destination_button);

        polylines = new ArrayList<>();

        // Set Pickup and Destination
        mSetDestinationSection = (LinearLayout) findViewById(R.id.set_destination_section);

        mSetLocation = (CardView) findViewById(R.id.location_section);
        mLocationInfoSection = (CardView) findViewById(R.id.location_info_display_section);

        mPlace_Location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        mPlace_Destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);

        //filter to restrict google places api to city
        mTypeFilter = new AutocompleteFilter.Builder()
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                        .setTypeFilter(3)
                        .build();

        mCancel = (ImageView) findViewById(R.id.cancel);
        mEditInfo = (ImageView) findViewById(R.id.location_info_edit);

        mPickUpText = (TextView) findViewById(R.id.pickup_info_text);
        mDestinationText = (TextView) findViewById(R.id.destination_info_text);

        mSetDestinationName = (TextView) findViewById(R.id.set_destination_section_name_text);

   //     mPlace_Location.setText("Current Location");


        mPlace_Location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //remove any old markers
                mMap.clear();

                //add new pickup marker when destination is set


                pickupMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Pickup Here")
                        .title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.newpickupmarker2)));

                //animate Camera Zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),17.0f));

                //getting info about Selected Place
                mPickUpName = place.getName().toString();

                pickupLatLng = place.getLatLng();// Getting Lat and Lng of Pick up

                //add pickup location
                //PickupLocation = place.getLatLng();

                mPickUpText.setText(mPickUpName);



            }

            @Override
            public void onError(Status status) {

            }
        });

        mPlace_Destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                //new destination marker


                destinationMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.destinationmarker2)));

                //animate Camera Zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),17.0f));

                //Get info about Selected Place
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng(); //Get Latitude and Longitude of Destination

                getRouteToDestinationMarker(destinationLatLng);// add poly line from pickup to destination

                mDestinationText.setText(destination);

                //call functions after three seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call imgExpandable
                 //       imgExpandable.performClick();

                        mSetLocation.setVisibility(View.GONE);
                        mRideRequestSection.setVisibility(View.VISIBLE);
                        mLocationInfoSection.setVisibility(View.VISIBLE);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLatLng,17.0f));

                    }
                },3000); //time in milli seconds


            }

            @Override
            public void onError(Status status) {

            }
        });


        //Set destination button

        mSetDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //show autoplace fragment
                mSetLocation.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call imgExpandable
                        //       imgExpandable.performClick();

                        mSetDestinationSection.setVisibility(View.GONE);

                    }
                },1000); //time in milli seconds
            }
        });

        //Cancel Image in destination Card View
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pickupMarker!= null){
                    pickupMarker.remove();
                }

                if (destinationMarker!=null){
                    destinationMarker.remove();
                }

                mSetDestinationSection.setVisibility(View.VISIBLE);
                mSetLocation.setVisibility(View.GONE);

            }
        });

        //set Edit Image in Location info Section

        mEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                erasePolylines();
                pickupMarker.remove();
                destinationMarker.remove();

                mSetLocation.setVisibility(View.VISIBLE);
                mRideRequestSection.setVisibility(View.GONE);
                mLocationInfoSection.setVisibility(View.GONE);

            }
        });

        //Vehicle Type
        mShared = (ImageView) findViewById(R.id.hyke_shared_pic);
        mTaxi = (ImageView) findViewById(R.id.hyke_taxi_pic);
        mPersonal = (ImageView) findViewById(R.id.hyke_personal_pic);

        //Vehicle Type event
        mPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mPersonal.isPressed()){
                    mShared.setImageResource(R.drawable.hykesharedlogbw);
                    mTaxi.setImageResource(R.drawable.hyketaxilogobw);
                    mPersonal.setImageResource(R.drawable.hykepersonallogo);
                } else {
                    mShared.setImageResource(R.drawable.hykesharedlogbw);
                    mTaxi.setImageResource(R.drawable.hyketaxilogobw);
                    mPersonal.setImageResource(R.drawable.hykepersonallogobw);

                }

                mRequest.setText("Request a HyKe Personal Ride");


                //set request service string
                requestService = ("HyKePersonal");

            }
        });

        mShared.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mShared.isPressed()){
                    mShared.setImageResource(R.drawable.hykesharedlog);
                    mTaxi.setImageResource(R.drawable.hyketaxilogobw);
                    mPersonal.setImageResource(R.drawable.hykepersonallogobw);
                } else {
                    mShared.setImageResource(R.drawable.hykesharedlogbw);
                    mTaxi.setImageResource(R.drawable.hyketaxilogobw);
                    mPersonal.setImageResource(R.drawable.hykepersonallogobw);

                }

                mRequest.setText("Request a HyKe Shared Ride");

                requestService = ("HyKeShared");

            }
        });

        mTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (mTaxi.isPressed()){
                    mTaxi.setImageResource(R.drawable.hyketaxilogo);
                    mShared.setImageResource(R.drawable.hykesharedlogbw);
                    mPersonal.setImageResource(R.drawable.hykepersonallogobw);
                } else {
                    mTaxi.setImageResource(R.drawable.hyketaxilogobw);
                    mShared.setImageResource(R.drawable.hykesharedlogbw);
                    mPersonal.setImageResource(R.drawable.hykepersonallogobw);

                }

                mRequest.setText("Request a HyKe Taxi Ride");
                requestService = ("HyKeTaxi");

            }
        });


// Request Button Purpose: Send a Request for a ride to a nearby Driver


        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPickUpHere();


            }
        });

        updateFirebaseToken();

    }

    private void updateFirebaseToken() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokens = database.getReference(Common.token_table);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(token);
    }

    private void requestPickUpHere() {

        if (requestBol){
            endRide();


        }else{

            if (requestService == null){
                Toast.makeText(CustomerMapActivity.this, "Choose a Service", Toast.LENGTH_SHORT).show();
                return;


            }
            else{

                switch (requestService){
                    case ("HyKeShared"):
                        if (mShared.isPressed()){
                            requestService = ("HyKeShared");
                        }

                        break;
                    case ("HyKePersonal"):
                        if (mPersonal.isPressed()){
                            requestService = ("HyKePersonal");}

                        break;

                    case ("HyKeTaxi"):
                        if (mTaxi.isPressed()){
                            requestService = ("HyKeTaxi");
                        }

                        break;

                }

            }
                    /*int selectId = mRadioGroup.getCheckedRadioButtonId();

                    final RadioButton radioButton = (RadioButton) findViewById(selectId);

                    if (radioButton.getText() == null){
                        return; //if radio button is not selected don't allow user to proceed
                    }

                    requestService = radioButton.getText().toString(); //getting radio button selected text*/

            requestBol = true;

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
            GeoFire geoFire = new GeoFire(ref);
            if (mLastLocation== null){
                Toast.makeText(CustomerMapActivity.this, "Turn on Location", Toast.LENGTH_LONG).show(); //if we can't get location we set a toast
                // endRide();
                return;
            }
            //   if (mPlace_Location!=null){
            //     geoFire.setLocation(userId, new GeoLocation(pickupLatLng.latitude,pickupLatLng.longitude)); //the new code
            //}
            else{
                geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }

            //set Pick up location marker

            if (mLastLocation== null){
                Toast.makeText(CustomerMapActivity.this, "Turn on Location", Toast.LENGTH_LONG).show();
                endRide();
                return;
            }
            // if (mPlace_Location!=null){
            // pickupLocation = new LatLng(pickupLatLng.latitude,pickupLatLng.longitude); //get LatLng from Place auto fragment

            //   mRequest.setText("Getting your Driver...."); //if location is set change button tex
            // }
            else{
                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()); //if pick up has not been set in place fragment get users current location as pick up location
                //pickupLocation = pickupLatLng; // pickup location of place fragment


                mRequest.setText("Getting your Driver...."); //change button text after setting location

            }

            if(pickupMarker == null) {
                mMap.clear();

                pickupMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.newpickupmarker2)));
            } //add new marker on user current location

            //Add Ripple animation on pick up location
            RequestMapRipple();




            getClosestDriver(); //search for driver/ refer to function
        }
    }

    private void RequestMapRipple() {
        // add Map ripple animation around marker
        if (mPlace_Location!=null){
            mapRipple = new MapRipple(mMap, new LatLng(pickupLatLng.latitude,pickupLatLng.longitude),this); //put ripples around pick up location set in place auto fragment
       }else {
            mapRipple = new MapRipple(mMap, new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),this); ///user current location
        }

        mapRipple.withNumberOfRipples(2);
        mapRipple.withDurationBetweenTwoRipples(500);
        mapRipple.withDistance(500);
        mapRipple.withRippleDuration(1000);
        mapRipple.withTransparency(0.5f);
        mapRipple.withStrokeColor(Color.WHITE);

        mapRipple.startRippleMapAnimation();

    }

    private void getRouteToDestinationMarker(LatLng destinationLatLng) {
        if (destinationLatLng != null && pickupLatLng != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false) //no alternative routes set
                    .waypoints(pickupLatLng, destinationLatLng) //points for drawing map
                    .key("AIzaSyAaxWUlhVnc2HgmvGyqk_qbFtaSJHRRlVg") //google maps Api Key
                    .build();
            routing.execute();
        }
    }

    private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    private GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){

                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(driverMap.get("service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    if (driverFound = true) {
                                         Toast.makeText(CustomerMapActivity.this, "Driver found", Toast.LENGTH_LONG).show();

                                         //send Notification
                                        sendNotificationRequestToDriver(driverFoundID);
                                    }


                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);

                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    mRequest.setText("Looking for Driver Location....");
//                                    mRadioGroup.setVisibility(View.GONE);



                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void sendNotificationRequestToDriver(String Uid) {


        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_table);

        tokens.orderByKey().equalTo(driverFoundID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot postSnapShot:dataSnapshot.getChildren())
                        {
                            Token token = postSnapShot.getValue(Token.class); //Get Token object from database with Key

                            String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            String riderToken = FirebaseInstanceId.getInstance().getToken();
                            Notification data = new Notification(riderToken,json_lat_lng);
                            Sender content = new Sender(token.getToken(), data);

                            mService.sendMessage(content)
                                    .enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.body().success==1){
                                                Toast.makeText(CustomerMapActivity.this, "Ride Request sent", Toast.LENGTH_SHORT)
                                                        .show();
                                            }else {
                                                Toast.makeText(CustomerMapActivity.this, "Failed to send request!", Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ERROR", t.getMessage());

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    private Marker mDriverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

        /*-------------------------------------------- Map specific functions -----
    |  Function(s) getDriverLocation
    |
    |  Purpose:  Get's most updated driver location and it's always checking for movements.
    |
    |  Note:
    |	   Even though geofire is used to push the location of the driver we can use a normal
    |      Listener to get it's location with no problem.
    |
    |      0 -> Latitude
    |      1 -> Longitudde
    |
    *-------------------------------------------------------------------*/

    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");

        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    mRequest.setText("Driver Found: " );

                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat,locationLng);

                    if(mDriverMarker != null){
                        mDriverMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100){
                        mRequest.setText("Driver's Here");
                        mapRipple.stopRippleMapAnimation();
                    }else{
                        mRequest.setText("Driver Found: " + String.valueOf(distance) + " m away");
                    }

                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



    /*-------------------------------------------- getDriverInfo -----
    |  Function(s) getDriverInfo
    |
    |  Purpose:  Get all the user information that we can get from the user's database.
    |
    |  Note: --
    |
    *-------------------------------------------------------------------*/
    private void getDriverInfo(){
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("first name")!=null){
                        mDriverName.setText(dataSnapshot.child("first name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mDriverPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("car")!=null){
                        mDriverCar.setText(dataSnapshot.child("car").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

  private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide(){
        requestBol = false;
        if (geoQuery != null){
        geoQuery.removeAllListeners();}
        if (driverLocationRef != null && driveHasEndedRef != null){
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driveHasEndedRefListener);}

        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        if (destinationMarker != null){
            destinationMarker.remove();
        }

        //remove polyline marks
        erasePolylines();

        //Remove Ripple Effect

        if (mapRipple == null){
            Toast.makeText(CustomerMapActivity.this, "", Toast.LENGTH_SHORT).show();
        }else {
            mapRipple.stopRippleMapAnimation();
        }




        //Change Button text back
        mRequest.setText("Request a Hyke Ride");

        //Remove driver Info

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("Destination: --");
        mDriverProfileImage.setImageResource(R.mipmap.ic_default_user);


        mLocationInfoSection.setVisibility(View.GONE);
        mRideRequestSection.setVisibility(View.GONE);
        mSetDestinationSection.setVisibility(View.VISIBLE);
    }

    /*-------------------------------------------- Map specific functions -----
    |  Function(s) onMapReady, buildGoogleApiClient, onLocationChanged, onConnected
    |
    |  Purpose:  Find and update user's location.
    |
    |  Note:
    |	   The update interval is set to 1000Ms and the accuracy is set to PRIORITY_HIGH_ACCURACY,
    |      If you're having trouble with battery draining too fast then change these to lower values
    |
    |
    *-------------------------------------------------------------------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //changing map style to Night style
        try {
          boolean isSuccess = googleMap.setMapStyle(
                  MapStyleOptions.loadRawResourceStyle(this, R.raw.my_map_style)
          );

          if (!isSuccess)
              Log.e("ERROR","Map Style Failed to Load!");
        }
        catch (Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }


        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

    }


    private static int number_calls;
    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){
                    mLastLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

                 /*   if (number_calls++ ==1){

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    } */



                  /*  if (mLastLocation!=null){
                        if(pickupMarker != null){
                            pickupMarker.remove();
                        }
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.drawable.newpickupmarker2)));
                    } */


                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));

                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                    if(!getDriversAroundStarted)
                         getDriversAround();
                }

            }

            //Restrict service
            LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
            LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);

            LatLngBounds bounds = LatLngBounds.builder()
                    .include(northSide)
                    .include(southSide)
                    .build();

            mPlace_Location.setBoundsBias(bounds);
            mPlace_Location.setFilter(mTypeFilter);

            mPlace_Destination.setBoundsBias(bounds);
            mPlace_Destination.setFilter(mTypeFilter);

        }
    };

    /*-------------------------------------------- onRequestPermissionsResult -----
    |  Function onRequestPermissionsResult
    |
    |  Purpose:  Get permissions for our app if they didn't previously exist.
    |
    |  Note:
    |	requestCode: the nubmer assigned to the request that we've made. Each
    |                request has it's own unique request code.
    |
    *-------------------------------------------------------------------*/
    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(CustomerMapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }




    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getDriversAround(){
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }

                LatLng driverLocation = new LatLng(location.latitude, location.longitude);

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car)));
                mDriverMarker.setTag(key);

                markers.add(mDriverMarker);


            }

            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

//Navigation Drawer

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            CustomerMapActivity.super.onBackPressed();
                        }
                    })
                    .show();

        }

    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu){
      //  getMenuInflater().inflate(R.menu.nav_drawer, menu);
      //  return true;
    //}

   // @Override
    //public boolean onOptionsItemSelected(MenuItem item){
      //  int id = item.getItemId();

        //if (id == R.id.action_settings) {
          //  return true;
        //}
     //   return super.onOptionsItemSelected(item);
   // }

    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.history){
            Intent intent = new Intent(CustomerMapActivity.this, HistoryActivity.class);
            intent.putExtra("customerOrDriver", "Customers");
            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }else if (id == R.id.settings){
            Intent searchIntent = new Intent(CustomerMapActivity.this, CustomerSettingsActivity.class);
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        }else if (id == R.id.change_password){
                    showDialogChangePwd();

        }else if (id == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(CustomerMapActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //creating change password dialog box
    private void showDialogChangePwd() {

        //call AlertDialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(CustomerMapActivity.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill in all Information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null);

        final MaterialEditText edtPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatNewPassword = (MaterialEditText) layout_pwd.findViewById(R.id.edtRepeatPassword);

        alertDialog.setView(layout_pwd);

        //Call Button and function

        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                final android.app.AlertDialog waitingDialog = new SpotsDialog(CustomerMapActivity.this);
                waitingDialog.show();

                if (edtNewPassword.getText().toString().equals(edtRepeatNewPassword.getText().toString()))
                {
                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    //gettin auth credentials from the user for re-authentication

                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatNewPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){

                                                            //Update password information Column
                                                            Map<String,Object> password = new HashMap<>();

                                                            password.put("password", edtRepeatNewPassword.getText().toString());

                                                            DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
                                                            customerReference.updateChildren(password)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                                Toast.makeText(CustomerMapActivity.this, "Password was changed Successfully", Toast.LENGTH_SHORT).show();
                                                                            else
                                                                                Toast.makeText(CustomerMapActivity.this, "Password was changed but not updated to Customer Information", Toast.LENGTH_SHORT).show();

                                                                            waitingDialog.dismiss();

                                                                        }
                                                                    });



                                                        }else {
                                                            Toast.makeText(CustomerMapActivity.this, "Password could not be changed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(CustomerMapActivity.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }else{
                    waitingDialog.dismiss();
                    Toast.makeText(CustomerMapActivity.this, "Password does not Match", Toast.LENGTH_SHORT).show();
                }


            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //remove dialogue
                dialogInterface.dismiss();
            }
        });

        //show dialog
        alertDialog.show();
    }

    //polylines on google maps
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.wallet_holo_blue_light};
    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }



    //Getting the driver closest to the customer
  /*  private int radius = 1;
    private Boolean driverFound = false;
    private String driverFoundID;

    private GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(requestService==null && driverMap.get("service").equals(requestService)){
                                    driverFound = true;
                                    driverFoundID = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID).child("customerRequest");
                                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customerRideId", customerId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatLng.latitude);
                                    map.put("destinationLng", destinationLatLng.longitude);
                                    driverRef.updateChildren(map);

                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    erasePolylines();
                                    mRequest.setText("Looking for Driver Location....");
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    } */


}

