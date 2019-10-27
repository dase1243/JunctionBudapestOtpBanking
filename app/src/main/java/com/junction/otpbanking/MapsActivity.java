package com.junction.otpbanking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static com.junction.otpbanking.AtmDB.atmDB;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public static long time;
    public static int count = 0;

    private GoogleMap mMap;
    private float zoomLevel = 13.5f; //This goes up to 21

    private TextView ibScanBarcode;
    private TextView ibListAtms;
    private TextView ibMapView;
    private TextView ibSideMenu;
    private Button btnHasCome;

    private Polyline mPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        time = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnHasCome = findViewById(R.id.btnHasCome);
        btnHasCome.setOnClickListener(v -> {
            boolean doesWantToTake = getIntent().getBooleanExtra("take", false);
            boolean doesWantToPut = getIntent().getBooleanExtra("put", false);
            double amount = getIntent().getDoubleExtra("amount", -1);
            Map<String, Atm> filteredAtms = atmDB.filterAtms(doesWantToPut, doesWantToTake, amount);

            filteredAtms = filteredAtms.entrySet().stream()
                    .sorted((o1, o2) -> {
                        long routeTiming1 = o1.getValue().getRouteTiming();
                        long routeTiming2 = o2.getValue().getRouteTiming();
                        long awaitingTime1 = o1.getValue().getLineCount()[0] * 90 - routeTiming1;
                        long awaitingTime2 = o2.getValue().getLineCount()[0] * 90 - routeTiming2;
                        return Long.compare(routeTiming1 + (awaitingTime1 < 0 ? 0 : awaitingTime1), routeTiming2 + (awaitingTime2 < 0 ? 0 : awaitingTime2));
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

//            String highlightKey = filteredAtms.entrySet().stream().findFirst().get().getKey();

            String highlightKey;
            String atmKey = getIntent().getStringExtra("atmKey");

            if (atmKey != null) {
                highlightKey = atmKey;
            } else {
                highlightKey = filteredAtms.entrySet().stream().findFirst().get().getKey();
            }

            Location closestAtm = new Location(highlightKey);
            Atm atm = atmDB.getAllAtms().get(highlightKey);
            closestAtm.setLatitude(atm.getLatitude());
            closestAtm.setLatitude(atm.getLongitude());
            float distance = atmDB.getLocation().distanceTo(closestAtm);
            if (distance > 5) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                builder.setMessage("It seems you far from the atm. Do you want to continue the trip?");
                builder.setPositiveButton("Continue", (dialog, which) -> dialog.cancel());

                builder.setNegativeButton("I have come", (dialog, which) -> {
                    continueToAtmOperations();
                    dialog.cancel();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                continueToAtmOperations();
            }

        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        menuListener();
        makeSchedule();
    }

    @Override
    public void onBackPressed() {

        finish();
        Intent intent = new Intent(getApplicationContext(), ListingActivity.class);
        intent.putExtra("take", getIntent().getBooleanExtra("take", false));
        intent.putExtra("put", getIntent().getBooleanExtra("put", false));
        intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
        finish();
        startActivity(intent);
    }

    private void makeSchedule() {
        final Handler handler = new Handler();
        Context context = this;
        Timer timer = new Timer();
        TimerTask doTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    try {
                        mMap.clear();
                        boolean doesWantToTake = getIntent().getBooleanExtra("take", false);
                        boolean doesWantToPut = getIntent().getBooleanExtra("put", false);
                        double amount = getIntent().getDoubleExtra("amount", -1);
                        Map<String, Atm> filteredAtms = atmDB.filterAtms(doesWantToPut, doesWantToTake, amount);

                        filteredAtms = filteredAtms.entrySet().stream()
                                .sorted((o1, o2) -> {
                                    long routeTiming1 = o1.getValue().getRouteTiming();
                                    long routeTiming2 = o2.getValue().getRouteTiming();
                                    long awaitingTime1 = o1.getValue().getLineCount()[count] * 90 - routeTiming1;
                                    long awaitingTime2 = o2.getValue().getLineCount()[count] * 90 - routeTiming2;
                                    return Long.compare(routeTiming1 + (awaitingTime1 < 0 ? 0 : awaitingTime1), routeTiming2 + (awaitingTime2 < 0 ? 0 : awaitingTime2));
                                })
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

                        if (count < 28) {
                            count++;
                        }

//                        String highlightKey = filteredAtms.entrySet().stream().findFirst().get().getKey();

                        String highlightKey;
                        String atmKey = getIntent().getStringExtra("atmKey");

                        if (atmKey != null) {
                            highlightKey = atmKey;
                        } else {
                            highlightKey = filteredAtms.entrySet().stream().findFirst().get().getKey();
                        }

                        for (Map.Entry<String, Atm> atmEntry : filteredAtms.entrySet()) {
                            Atm atm = atmEntry.getValue();
                            LatLng latLng = new LatLng(atm.getLatitude(), atm.getLongitude());

                            MarkerOptions marker = new MarkerOptions()
                                    .position(latLng)
                                    .title("Id: " + atm.getId());

                            if (highlightKey.equals(atmEntry.getKey())) {
                                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }

                            mMap.addMarker(marker);
                        }

                        LatLng latLng = new LatLng(atmDB.getLocation().getLatitude(), atmDB.getLocation().getLongitude());

                        drawRoute(latLng, new LatLng(atmDB.getAllAtms().get(highlightKey).getLatitude(), atmDB.getAllAtms().get(highlightKey).getLongitude()));

                        MarkerOptions marker = new MarkerOptions()
                                .icon(bitmapDescriptorFromVector(context, R.drawable.ic_map_marker))
                                .position(latLng)
                                .title("My location");

                        mMap.addMarker(marker);


                        Location closestAtm = new Location(highlightKey);
                        Atm atm = atmDB.getAllAtms().get(highlightKey);
                        closestAtm.setLatitude(atm.getLatitude());
                        closestAtm.setLatitude(atm.getLongitude());
                        float distance = atmDB.getLocation().distanceTo(closestAtm);
                        if (distance <= 5) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                            builder.setMessage("It seems you have come. Do you want to continue to operations?");
                            builder.setPositiveButton("Continue", (dialog, which) -> {
                                        continueToAtmOperations();
                                        dialog.cancel();
                                    }
                            );

                            builder.setNegativeButton("I want to build another route", (dialog, which) -> {
                                dialog.cancel();
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Log.w("warning", e.getMessage());
                    }
//                    Intent intent = getIntent();
//                    finish();
//                    overridePendingTransition(0, 0);
//                    startActivity(intent);
//                    overridePendingTransition(0, 0);
                });
            }
        };
        timer.schedule(doTask, 1000);
    }

    private void continueToAtmOperations() {
        Intent intent = new Intent(getApplicationContext(), FinishTripActivity.class);
        intent.putExtra("take", getIntent().getBooleanExtra("take", false));
        intent.putExtra("put", getIntent().getBooleanExtra("put", false));
        intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
        finish();
        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double latitude = 0;
        double longitude = 0;

        boolean doesWantToTake = getIntent().getBooleanExtra("take", false);
        boolean doesWantToPut = getIntent().getBooleanExtra("put", false);
        double amount = getIntent().getDoubleExtra("amount", -1);
        Map<String, Atm> filteredAtms = atmDB.filterAtms(doesWantToPut, doesWantToTake, amount);

        filteredAtms = filteredAtms.entrySet().stream()
                .sorted((o1, o2) -> {
                    long routeTiming1 = o1.getValue().getRouteTiming();
                    long routeTiming2 = o2.getValue().getRouteTiming();
                    long awaitingTime1 = o1.getValue().getLineCount()[0] * 90 - routeTiming1;
                    long awaitingTime2 = o2.getValue().getLineCount()[0] * 90 - routeTiming2;
                    return Long.compare(routeTiming1 + (awaitingTime1 < 0 ? 0 : awaitingTime1), routeTiming2 + (awaitingTime2 < 0 ? 0 : awaitingTime2));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

//        String highlightKey = filteredAtms.entrySet().stream().findFirst().get().getKey();

        String highlightKey;
        String atmKey = getIntent().getStringExtra("atmKey");

        if (atmKey != null) {
            highlightKey = atmKey;
        } else {
            highlightKey = filteredAtms.entrySet().stream().findFirst().get().getKey();
        }

        for (Map.Entry<String, Atm> atmEntry : filteredAtms.entrySet()) {
            Atm atm = atmEntry.getValue();
            LatLng latLng = new LatLng(atm.getLatitude(), atm.getLongitude());

            MarkerOptions marker = new MarkerOptions()
//                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_map_marker))
                    .position(latLng)
                    .title("Id: " + atm.getId());

            if (highlightKey.equals(atmEntry.getKey())) {
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            mMap.addMarker(marker);

            latitude += atm.getLatitude();
            longitude += atm.getLongitude();
        }

        //add current location

        LatLng latLng = new LatLng(atmDB.getLocation().getLatitude(), atmDB.getLocation().getLongitude());

        drawRoute(latLng, new LatLng(atmDB.getAllAtms().get(highlightKey).getLatitude(), atmDB.getAllAtms().get(highlightKey).getLongitude()));

        latitude += atmDB.getLocation().getLatitude();
        longitude += atmDB.getLocation().getLongitude();

        MarkerOptions marker = new MarkerOptions()
                .icon(bitmapDescriptorFromVector(this, R.drawable.ic_map_marker))
                .position(latLng)
                .title("My location");

        mMap.addMarker(marker);

        final Context context = this;
        mMap.setOnInfoWindowClickListener(arg0 -> {
            // call an activity(xml file)
            //todo: pop up with info about this atm
//            Intent intent = new Intent(context, TransformerBoxActivity.class);
//            intent.putExtra("equino", arg0.getTitle().split(" ")[1]);
//            finish();
//            startActivity(intent);
        });

        int size = atmDB.getAllAtms().size();

        LatLng avgLatLng = new LatLng(latitude / (size + 1), longitude / (size + 1));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(avgLatLng, zoomLevel));
        new CurrentLocationListener(getApplicationContext());
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void menuListener() {
        ibListAtms = findViewById(R.id.ibListTransformerBoxes);
        ibMapView = findViewById(R.id.ibMapView);
        ibSideMenu = findViewById(R.id.ibSideMenu);

        ibScanBarcode = findViewById(R.id.ibScanBarcode);

        ibScanBarcode.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "When ready to scan - tap", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
            intent.putExtra("take", getIntent().getBooleanExtra("take", false));
            intent.putExtra("put", getIntent().getBooleanExtra("put", false));
            intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
            startActivity(intent);
        });

        ibListAtms.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ListingActivity.class);
            intent.putExtra("take", getIntent().getBooleanExtra("take", false));
            intent.putExtra("put", getIntent().getBooleanExtra("put", false));
            intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
            startActivity(intent);
            finish();
        });

        ibMapView.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Load map", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("take", getIntent().getBooleanExtra("take", false));
            intent.putExtra("put", getIntent().getBooleanExtra("put", false));
            intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
            startActivity(intent);
        });

        ibSideMenu.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Side settings", Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(getApplicationContext(), DistanceUpdateActivity.class);
//            intent.putExtra("equino", equino);
//            startActivity(intent);
        });
    }


    private void drawRoute(LatLng mOrigin, LatLng mDestination) {

        // Getting URL to the Google Directions API
//        String url = getDirectionsUrl(mOrigin, mDestination);

        String url = "https://maps.googleapis.com" + "/maps/api/directions/json?" +
                "origin=" + atmDB.getLocation().getLatitude() + "," + atmDB.getLocation().getLongitude() +
                "&destination=" + mDestination.latitude + "," + mDestination.longitude +
                "&mode=walking" +
                "&key=AIzaSyA04qYRWy_mvo7Qbbjt0Y_7wz4xyzlz7SQ";

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&amp;" + str_dest + "&amp;" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Directions in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }
                mPolyline = mMap.addPolyline(lineOptions);

            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }


}
