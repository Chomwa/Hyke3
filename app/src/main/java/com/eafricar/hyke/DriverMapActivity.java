package com.eafricar.hyke;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.eafricar.hyke.Common.Common;
import com.eafricar.hyke.Model.Token;
import com.eafricar.hyke.Remote.IFCMservice;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.BufferedInputStream;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.eafricar.hyke.Common.Common.mLastLocation;

public class DriverMapActivity extends AppCompatActivity implements OnMapReadyCallback, RoutingListener, NavigationView.OnNavigationItemSelectedListener{

    private GoogleMap mMap;

    LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationClient;


    private Button  mRideStatus,  mSetRoute, mDone;

    private Switch mWorkingSwitch;

    private int status = 0;

    private String customerId = "", destination;
    private LatLng destinationLatLng, pickupLatLng, driverDestinationLatLng, driverStartLatLng;
    private float rideDistance;

    private Boolean isLoggingOut = false;

    private FrameLayout mDriverLayout;

    private SupportMapFragment mapFragment;

    //Assigned Customer information section
    private LinearLayout mCustomerInfo;

    private ImageView mCustomerProfileImage;

    private TextView mCustomerName, mCustomerPhone, mCustomerDestination,
            mNavigationHeaderTextFirstName,mNavigationHeaderTextLastName,
            mNavigationHeaderTextPhoneNumber;

    private DatabaseReference mDriverDatabase;
    private String userID;

    private String mNavigationHeaderImageUrl, mName;

    //tool bar variables
    private Toolbar mToolbar;
    private ImageView mToggleImage, NavigationHeaderImage;
    private DrawerLayout mDrawer;

    //place auto complete fragment
    private PlaceAutocompleteFragment mPlace_Location, mPlace_Destination;

    private Marker DriverDestinationMarker, DriverPickupMarker;
    private String mDriverPickup, mDriverDestination;

    //Passenger payment and Number
    private EditText mPassengerNumber, mPassengerCharge;

    //Notification Service

    private IFCMservice mService;

    private GeoFire geofire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mService = Common.getFCMService();

        mDriverLayout = (FrameLayout) findViewById(R.id.driverMapLayout);

        //Calling tool bar and tool bar functions
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//removing Title from the transparent tool bar

        mToggleImage = (ImageView) findViewById(R.id.toolbarprofileImage); //Calling Image that will serve as toggle
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);

        //calling toolbar toggle function in Image
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
        navigationView.setNavigationItemSelectedListener(this);

        //calling Navigation Drawer Header Variables
        View header = navigationView.getHeaderView(0);
        NavigationHeaderImage = (ImageView) header.findViewById(R.id.profileImage); //profile image
        mNavigationHeaderTextFirstName = (TextView) header.findViewById(R.id.navigationtextFirstName); //First name of User
        mNavigationHeaderTextLastName = (TextView) header.findViewById(R.id.navigationtextLastName); //Last Name of User
        mNavigationHeaderTextPhoneNumber = (TextView) header.findViewById(R.id.navigationtextPhoneNumber); //Phone Number of User

        //Database reference in relation to navigation header image
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userID);

        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                    if(map.get("profileImageUrl")!=null){
                        mNavigationHeaderImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mNavigationHeaderImageUrl).into(NavigationHeaderImage); //calling user profile picture into navigation header profile picture
                    }
             /*       if(map.get("profileImageUrl")!=null){
                        mNavigationHeaderImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mNavigationHeaderImageUrl).into(mToggleImage); // calling user profile picture into Toggle profile picture
                    } */

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("firstName")!=null){
           //             mNavigationHeaderTextFirstName.setText(dataSnapshot.child("first name").getValue().toString()); //navigation user first name
                    }
                    if(dataSnapshot.child("lastName")!=null){
             //           mNavigationHeaderTextLastName.setText(dataSnapshot.child("last name").getValue().toString()); //navigation user last name
                    }
                    if(dataSnapshot.child("phoneNumber")!=null){
               //         mNavigationHeaderTextPhoneNumber.setText(dataSnapshot.child("phone").getValue().toString()); //navigation user phone number
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        polylines = new ArrayList<>();//Google maps poly lines list

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //Calling Customer Information Variables when receiving a customer ride request
        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);// Customer info Linear layout which is currently hidden

        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);

        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        mCustomerDestination = (TextView) findViewById(R.id.customerDestination);

        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch); //Calling switch to enable driver to go online and go offline
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    connectDriver(); //refer to function
                    Snackbar.make(mDriverLayout, "You're now Online", Snackbar.LENGTH_SHORT)
                            .show();
                }else{
                    disconnectDriver(); //refer to function

                    Snackbar.make(mDriverLayout, "You're now Offline", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });


        mRideStatus = (Button) findViewById(R.id.rideStatus); //calling before and After customer is to be picked up

        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolylines(); //remove existing poly lines on map
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
                            getRouteToMarker(destinationLatLng); // set new poly line
                        }
                        mRideStatus.setText("drive completed");//Changing Button text

                        break;
                    case 2:
                        recordRide(); //refer to function
                        endRide(); //refer to function
                        break;
                }
            }
        });

      /*  // Set Pickup and Destination
        final CardView locationCardView = (CardView) findViewById(R.id.route);
        final CardView passengerDetailsCardView = (CardView) findViewById(R.id.passengerDetails);

        mPlace_Location = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_location);
        mPlace_Destination = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_destination);

        mSetRoute = (Button) findViewById(R.id.set_route);

        driverDestinationLatLng = new LatLng(0.0,0.0);
        driverStartLatLng = new LatLng(0.0, 0.0);

        //event
        mPlace_Location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //remove any old markers
                mMap.clear();

                //add new pickup marker when destination is set

                DriverPickupMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("Pickup Here")
                        .title("Pickup Here").icon(BitmapDescriptorFactory.defaultMarker()));

                //animate Camera Zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),17.0f));

                //getting info about Selected Place
                mDriverPickup = place.getName().toString();

              //  driverStartLatLng = place.getLatLng();// Getting Lat and Lng of Pick up

                //add pickup location
                //PickupLocation = place.getLatLng();

            }

            @Override
            public void onError(Status status) {

            }
        });

        mPlace_Destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                //new destination marker
                DriverDestinationMarker = mMap.addMarker(new MarkerOptions().position(place.getLatLng())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                //animate Camera Zoom
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),17.0f));

                //Get info about Selected Place
                mDriverDestination = place.getName().toString();
                driverDestinationLatLng = place.getLatLng(); //Get Latitude and Longitude of Destination

                //getRouteToDestinationMarker(destinationLatLng);// add poly line from pickup to destination

                mSetRoute.setVisibility(View.VISIBLE);

            }

            @Override
            public void onError(Status status) {

            }
        });

        mSetRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRouteToDriverDestination(driverDestinationLatLng);

               locationCardView.setVisibility(View.GONE);

                //call Payment and Passenger Cardview after two seconds
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //call Card view

                        passengerDetailsCardView.setVisibility(View.VISIBLE);

                    }
                },1000); //time in milli seconds
            }
        });

        //Set Passenger Charge and Number of customers to carry
        mPassengerNumber = (EditText) findViewById(R.id.passenger_number);
        mPassengerCharge = (EditText) findViewById(R.id.passenger_charge);

        mDone = (Button) findViewById(R.id.set_passenger_details);

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get Text of passenger charge and number
                mPassengerCharge.getText().toString();
                mPassengerNumber.getText().toString();

                //updateUI
                passengerDetailsCardView.setVisibility(View.GONE);
                mDone.setVisibility(View.GONE);


                //turn on working switch
                if (!mWorkingSwitch.isChecked()){
                    mWorkingSwitch.performClick();
                }

            }
        });  */


      updateFirebaseToken();
        getAssignedCustomer();// Call customer when working switch is turned on/ refer to function
    }

    private void updateFirebaseToken() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tokens = database.getReference(Common.token_table);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);
    }

    private void getAssignedCustomer(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest").child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                    getAssignedCustomerDestination();
                    getAssignedCustomerInfo();
                }else{
                    endRide();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    Marker pickupMarker;
    private DatabaseReference assignedCustomerPickupLocationRef;
    private ValueEventListener assignedCustomerPickupLocationRefListener;
    private void getAssignedCustomerPickupLocation(){
        assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerPickupLocationRefListener = assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if(map.get(0) != null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1) != null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    pickupLatLng = new LatLng(locationLat,locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions()
                            .position(pickupLatLng)
                            .title("pickup location")
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));

                    getRouteToMarker(pickupLatLng);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .key("AIzaSyAaxWUlhVnc2HgmvGyqk_qbFtaSJHRRlVg")
                    .build();
            routing.execute();
        }
    }

 /*   private void getRouteToDriverDestination(LatLng driverDestinationLatLng) {
        if (driverDestinationLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(driverStartLatLng, driverDestinationLatLng)
                    .key("AIzaSyAaxWUlhVnc2HgmvGyqk_qbFtaSJHRRlVg")
                    .build();
            routing.execute();
        }
    } */

    private void getAssignedCustomerDestination(){
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("destination")!=null){
                        destination = map.get("destination").toString();
                        mCustomerDestination.setText("Destination: " + destination);
                    }
                    else{
                        mCustomerDestination.setText("Destination: --");
                    }

                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if(map.get("destinationLat") != null){
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if(map.get("destinationLng") != null){
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void getAssignedCustomerInfo(){
        mCustomerInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("firstName")!=null){
                        mCustomerName.setText(map.get("firstName").toString());
                    }
                    if(map.get("phoneNumber")!=null){
                        mCustomerPhone.setText(map.get("phoneNumber").toString());
                    }
                    if(map.get("profileImageUrl")!=null){
                        Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mCustomerProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void endRide(){
        mRideStatus.setText("picked customer");
        erasePolylines();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance = 0;

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (assignedCustomerPickupLocationRefListener != null){
            assignedCustomerPickupLocationRef.removeEventListener(assignedCustomerPickupLocationRefListener);
        }
        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        mCustomerDestination.setText("Destination: --");
        mCustomerProfileImage.setImageResource(R.mipmap.ic_default_user);
    }

    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(userId).child("history");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);
    }

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }

    //Getting Google maps Started
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
        mLocationRequest.setInterval(1000); //setting refreshing interval to 1 sec
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);// setting phone priority to high

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission(); //Calling location permissions
            }
        }
    }


    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()){
                if(getApplicationContext()!=null){

                    if(!customerId.equals("") && mLastLocation!=null && location != null){
                        rideDistance += mLastLocation.distanceTo(location)/1000; //getting ride distance
                    }
                    mLastLocation = location;


                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng)); //moving camera to location
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));//Zooming in on google maps

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
                    DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
                    GeoFire geoFireAvailable = new GeoFire(refAvailable);
                    GeoFire geoFireWorking = new GeoFire(refWorking);

                    //switching driver from available to working child once customer is received
                    switch (customerId){
                        case "":
                            geoFireWorking.removeLocation(userId);
                            geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;

                        default:
                            geoFireAvailable.removeLocation(userId);
                            geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                    }
                }
            }
        }
    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                    .create()
                    .show();
            }
            else{
                ActivityCompat.requestPermissions(DriverMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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





    private void connectDriver(){
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    //remove driver from drivers available child
    private void disconnectDriver(){
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }





    //creating poly lines
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.wallet_holo_blue_light};// only set up one color in list
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
    //removepoly line function
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    // Navigation Drawer functions
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DriverMapActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }


    }

    //@Override
    //public boolean onCreateOptionsMenu(Menu menu){
      //  getMenuInflater().inflate(R.menu.nav_drawer, menu);
        //return true;
    //}

   // @Override
    //public boolean onOptionsItemSelected(MenuItem item){
      //  int id = item.getItemId();

        //if (id == R.id.action_settings) {
        //    return true;
       // }
       // return super.onOptionsItemSelected(item);
  //  }

    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.history){
            Intent intent = new Intent(DriverMapActivity.this, HistoryActivity.class); // intent to ride history class
            intent.putExtra("customerOrDriver", "Drivers");
            startActivity(intent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left); //effect of sliding when accessing new class
        }else if (id == R.id.settings){
            Intent searchIntent = new Intent(DriverMapActivity.this, DriverSettingsActivity.class); //intent to driver profile
            startActivity(searchIntent);
            overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
            //Log out function
        }else if (id == R.id.logout){
            isLoggingOut = true;

            disconnectDriver(); //refer to function

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DriverMapActivity.this, MainActivity.class); //return to main activity page
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout); //calling the drawer layout
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}