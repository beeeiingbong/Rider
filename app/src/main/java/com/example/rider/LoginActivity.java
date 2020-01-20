package com.example.rider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class LoginActivity extends AppCompatActivity {

    public void redirectActivity(){
        if (ParseUser.getCurrentUser().get("RiderorDriver").equals("rider")){
            Intent intent = new Intent(this,RiderActivity.class);
            startActivity(intent);
        }

        else{

            Intent intent = new Intent(this,ViewRequestsActivity.class);
            startActivity(intent);

        }
    }

    public void getStarted(View view){

        Switch userTypeSwitch =(Switch) findViewById(R.id.userTypeSwitch);

        Log.i("Switch Value", String.valueOf(userTypeSwitch.isChecked()));

        String userType="rider";

        if(userTypeSwitch.isChecked()){
            userType="driver";
        }

        ParseUser.getCurrentUser().put("RiderorDriver", userType);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                redirectActivity();
            }
        });

//        Log.i("Info","Redirecting as " + userType);
//        redirectActivity();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        if(ParseUser.getCurrentUser() == null){
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if(e == null){

                        Log.i("Info","Anonymous login successful");

                    }

                    else{

                        Log.i("Info", "Anonymous login failed");
                    }
                }
            });

        }

        else{
            if (ParseUser.getCurrentUser().get("RiderorDriver") != null){
                Log.i("Info", "Redirecting as "+ ParseUser.getCurrentUser().get("RiderorDriver"));
                redirectActivity();
            }
        }

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}
