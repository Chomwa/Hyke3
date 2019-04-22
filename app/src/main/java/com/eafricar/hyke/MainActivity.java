package com.eafricar.hyke;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private ImageButton mDriver, mCustomer;

    private TextView mTapOnIcon;

    private LinearLayout mChooseOption;

    private ProgressBar pgsBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pgsBar = findViewById(R.id.pBar);

        mDriver = (ImageButton) findViewById(R.id.driver);
        mCustomer = (ImageButton) findViewById(R.id.customer);
        mChooseOption = (LinearLayout) findViewById(R.id.choose_option_section);

        mTapOnIcon = (TextView) findViewById(R.id.tap_icon);

        startService(new Intent(MainActivity.this, onAppKilled.class));
        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        pgsBar.setVisibility(View.VISIBLE);
        checkInternetConnectivity();
    }

    private void checkInternetConnectivity() {


        if (NetworkAppStatus.getInstance(this).isOnline()){
            mChooseOption.setVisibility(View.VISIBLE);
            mTapOnIcon.setVisibility(View.VISIBLE);
            pgsBar.setVisibility(View.GONE);
        }else{
            pgsBar.setVisibility(View.GONE);
           new AlertDialog.Builder(this)
                   .setMessage("No internet Connection. Please try again later")
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           finish();
                       }
                   })
                   .show();

        }


    }

}
