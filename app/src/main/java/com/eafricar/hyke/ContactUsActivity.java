package com.eafricar.hyke;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ContactUsActivity extends AppCompatActivity {

    private ImageView mPhonNumber, mEmailAddress, mLocationAddress, mWebsite,
            mFacebookImage,mInstagramImage, mTwitterImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        mPhonNumber = findViewById(R.id.dial_phone_number);
        mEmailAddress = findViewById(R.id.send_email);
        mFacebookImage = findViewById(R.id.facebook);
        mInstagramImage = findViewById(R.id.instagram);
        mTwitterImage = findViewById(R.id.twitter);


        mPhonNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialPhoneNumber();
            }
        });

        mEmailAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        mFacebookImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFacebookPage();
            }
        });

        mInstagramImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInstagramPpage();
            }
        });

        mTwitterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTwitterPage();
            }
        });
    }

    private void openInstagramPpage() {
        Intent instagramBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/hykehq"));
        startActivity(instagramBrowserIntent);
    }

    private void openTwitterPage() {
        Intent twitterBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/Hykehq"));
        startActivity(twitterBrowserIntent);
    }

    private void openFacebookPage() {
        Intent facebookBrowserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Hykehq/"));
        startActivity(facebookBrowserIntent);
    }

    private void sendEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto","hykehq@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Hello HyKe Team");
        startActivity(Intent.createChooser(emailIntent, "Choose an Email Client:"));
    }

    private void dialPhoneNumber() {
        Uri number = Uri.parse("tel:+260957595916");
        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
        startActivity(callIntent);
    }
}
