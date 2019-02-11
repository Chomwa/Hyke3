package com.eafricar.hyke;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class SettingsActivity extends AppCompatActivity {

    private TextView mChangePassword;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mChangePassword = (TextView) findViewById(R.id.change_password);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogChangePwd();
            }
        });


    }



    //creating change password dialog box
    private void showDialogChangePwd() {

        //call AlertDialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SettingsActivity.this);
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

                final android.app.AlertDialog waitingDialog = new SpotsDialog(SettingsActivity.this);
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
                                                                                Toast.makeText(SettingsActivity.this, "Password was changed Successfully", Toast.LENGTH_SHORT).show();
                                                                            else
                                                                                Toast.makeText(SettingsActivity.this, "Password was changed but not updated to Customer Information", Toast.LENGTH_SHORT).show();

                                                                            waitingDialog.dismiss();

                                                                        }
                                                                    });



                                                        }else {
                                                            Toast.makeText(SettingsActivity.this, "Password could not be changed", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                }else{
                    waitingDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "Password does not Match", Toast.LENGTH_SHORT).show();
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
}
