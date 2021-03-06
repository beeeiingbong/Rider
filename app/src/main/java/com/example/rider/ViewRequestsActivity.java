package com.example.rider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    ListView requestListView;
    ArrayList<String> requests = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    ArrayList<Double> requestLatitudes = new ArrayList<Double>();
    ArrayList<Double> requestLongitudes = new ArrayList<Double>();

    ArrayList<String> usernames = new ArrayList<String>();
    LocationManager locationManager;
    LocationListener locationListener;

    public void updateListView(Location location) {

        if (location != null) {


            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");

            final ParseGeoPoint geoPointLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            query.whereNear("location",geoPointLocation);

            query.whereDoesNotExist("driverUsername");

            query.setLimit(10);

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null){

                        requests.clear();
                        requestLatitudes.clear();
                        requestLongitudes.clear();

                        if(objects.size()>0){

                            for (ParseObject object : objects) {

                                ParseGeoPoint requestLocation = (ParseGeoPoint) object.get("location");

                                if (requestLocation != null) {
                                    Double distanceInMiles = (geoPointLocation.distanceInMilesTo((ParseGeoPoint) object.get("location")))*1.609;

                                    Double distanceOneDP = (double) (Math.round(distanceInMiles * 1.609 * 10) / 10);

                                    requests.add(distanceOneDP.toString() + "km");

                                    requestLatitudes.add(requestLocation.getLatitude());
                                    requestLongitudes.add(requestLocation.getLongitude());
                                    usernames.add(object.getString("username"));

                                }
                            }

                            arrayAdapter.notifyDataSetChanged();
                        }

                    }

                    else{

                        requests.add("No active requests nearby");
                    }

                    arrayAdapter.notifyDataSetChanged();
                }
            });

            arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);

            requestListView.setAdapter(arrayAdapter);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length >= 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    updateListView(lastknownlocation);

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        setTitle("Nearby Requests");

         requestListView = (ListView) findViewById(R.id.requestListView);

         arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, requests);

         requests.clear();

         requests.add("Getting nearby requets .... ");

         requestListView.setAdapter(arrayAdapter);

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//                if (ContextCompat.checkSelfPermission(ViewRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (requestLatitudes.size() > i && requestLongitudes.size() > i && usernames.size()> i && lastknownlocation != null ) {

                        String item= (String)adapterView.getItemAtPosition(i);
                        Toast.makeText(getApplicationContext(),"You are a at position "+ item,Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), DriverLocationActivity.class);

                        intent.putExtra("requestLatitude", requestLatitudes.get(i));
                        intent.putExtra("requestLongitude", requestLongitudes.get(i));
                        intent.putExtra("driverLatitude", lastknownlocation.getLatitude());
                        intent.putExtra("driverLongitude", lastknownlocation.getLongitude());
                        intent.putExtra("username",usernames.get(i));
                        startActivity(intent);


//                String item= (String)adapterView.getItemAtPosition(i);
//                Toast.makeText(getApplicationContext(),"You are a at position "+ item,Toast.LENGTH_SHORT).show();
//
//
//                        Intent intent = new Intent(getApplicationContext(), DriverLocationActivity.class);
//
//                        intent.putExtra("requestLaitude", requestLatitudes.get(i));
//                        intent.putExtra("requestLongitude", requestLongitudes.get(i));
//                        intent.putExtra("driverLatitude", lastknownlocation.getLatitude());
//                        intent.putExtra("driverLongitude", lastknownlocation.getLongitude());
//
//                        startActivity(intent);

            }
                }
//            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                updateListView(location);
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

            Location lastknownlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastknownlocation != null){
                updateListView(lastknownlocation);
            }
        }
    }
}




