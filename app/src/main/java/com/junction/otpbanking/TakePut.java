package com.junction.otpbanking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static com.junction.otpbanking.AtmDB.atmDB;

public class TakePut extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_put);

        Button btnTake = findViewById(R.id.btnTake);
        Button btnPut = findViewById(R.id.btnPut);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        requestAllPermissions();
        checkLocationStatus();

        updateLocationOnStart();

        new CurrentLocationListener(getApplicationContext());

        btnTake.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AmountActivity.class);
            intent.putExtra("take", true);
            startActivity(intent);
        });

        btnPut.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ListingActivity.class);
            intent.putExtra("put", true);
            startActivity(intent);
        });

//        Thread timings = new Thread() {
//            @Override
//            public void run() {
//            }
//        };
//
//        AsyncTask.execute(timings);

        atmDB.fillData(this);
        getAtmRouteTiming();
    }

    private void getAtmRouteTiming() {
        for (Map.Entry<String, Atm> atmEntry : atmDB.getAllAtms().entrySet()) {
            Atm atm = atmEntry.getValue();
            String baseUrl = "https://maps.googleapis.com";

            String url = baseUrl + "/maps/api/directions/json?" +
                    "origin=" + atmDB.getLocation().getLatitude() + "," + atmDB.getLocation().getLongitude() +
                    "&destination=" + atm.getLatitude() + "," + atm.getLongitude() +
                    "&mode=walking" +
                    "&key=AIzaSyA04qYRWy_mvo7Qbbjt0Y_7wz4xyzlz7SQ";

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    r -> {
                        try {
                            System.out.println("Parsing atm direction route distance timing");
                            JSONObject jsonObject = new JSONObject(r);
                            if (jsonObject.getString("status").equals("OK")) {
                                long timing = ((JSONObject) ((JSONObject) jsonObject.getJSONArray("routes").get(0)).getJSONArray("legs").get(0)).getJSONObject("duration").getInt("value");
                                atmDB.getAllAtms().get(atm.getId()).setRouteTiming(timing);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show());

            RequestQueue requestQueue = Volley.newRequestQueue(this);

            requestQueue.add(stringRequest);
        }
    }

    private void updateLocationOnStart() {
        LocationFinder finder;
        double longitude = 0.0, latitude = 0.0;
        finder = new LocationFinder(this);
        if (finder.canGetLocation()) {
            latitude = finder.getLatitude();
            longitude = finder.getLongitude();
//            Toast.makeText(this, "lat-lng " + latitude + "--" + longitude, Toast.LENGTH_LONG).show();

            Location location = new Location("1");
            location.setLongitude(longitude);
            location.setLatitude(latitude);

            atmDB.setLocation(location);
        } else {
            finder.showSettingsAlert();
        }
    }

    private void checkLocationStatus() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is deactivated. Do you want to switch it on?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void requestAllPermissions() {

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
