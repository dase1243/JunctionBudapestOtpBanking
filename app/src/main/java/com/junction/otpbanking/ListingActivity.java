package com.junction.otpbanking;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.junction.otpbanking.AtmDB.atmDB;

public class ListingActivity extends AppCompatActivity {
    private ListView listView;
    DatabaseReference mDatabase;
    static boolean firstStart = true;

    private TextView ibScanBarcode;
    private TextView ibListAtms;
    private TextView ibMapView;
    private TextView ibSideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        populateData();
        menuListener();
    }

    private void populateData() {

        boolean doesWantToTake = getIntent().getBooleanExtra("take", false);
        boolean doesWantToPut = getIntent().getBooleanExtra("put", false);
        double amount = getIntent().getDoubleExtra("amount", -1);
        Map<String, Atm> filteredAtms = atmDB.filterAtms(doesWantToPut, doesWantToTake, amount);

        filteredAtms = filteredAtms.entrySet().stream()
                .sorted((o1, o2) -> {
                    long routeTiming1 = o1.getValue().getRouteTiming();
                    long routeTiming2 = o2.getValue().getRouteTiming();
                    //todo: очинить на видео

                    Calendar calendar = Calendar.getInstance();
                    int h = calendar.get(Calendar.HOUR);
                    int m = calendar.get(Calendar.MINUTE);
                    int count = h * 2 + m / 30;
                    long awaitingTime1 = o1.getValue().getLineCount()[count] / 15 * 90 - routeTiming1;
                    long awaitingTime2 = o2.getValue().getLineCount()[count] / 15 * 90 - routeTiming2;
                    return Long.compare(routeTiming1 + (awaitingTime1 < 0 ? 0 : awaitingTime1), routeTiming2 + (awaitingTime2 < 0 ? 0 : awaitingTime2));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        AddressesListAdapter adapter = new AddressesListAdapter(this, R.layout.custom_list_layout, new ArrayList<>(filteredAtms.values()));

        listView = findViewById(R.id.listViewItems);

        listView.setAdapter(adapter);

//        if(firstStart) {
//            firstStart = false;
//            writeNewUser(filteredAtms.entrySet().stream().findFirst().get().getValue());
//        }
        //todo: make a route on map by clicking
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Atm atm = (Atm) parent.getAdapter().getItem(position);

            if (position != 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ListingActivity.this);

                builder.setMessage("You choose non optimal route. We can save your time.");
                builder.setPositiveButton("Save my time", (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

                    intent.putExtra("take", getIntent().getBooleanExtra("take", false));
                    intent.putExtra("put", getIntent().getBooleanExtra("put", false));
                    intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
                    startActivity(intent);
                });

                builder.setNegativeButton("I made a decision", (dialog, which) -> {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

                    intent.putExtra("take", getIntent().getBooleanExtra("take", false));
                    intent.putExtra("put", getIntent().getBooleanExtra("put", false));
                    intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
                    intent.putExtra("atmKey", atm.getId());

                    startActivity(intent);
                    dialog.cancel();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

                intent.putExtra("take", getIntent().getBooleanExtra("take", false));
                intent.putExtra("put", getIntent().getBooleanExtra("put", false));
                intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
                startActivity(intent);
            }
        });
    }

    private void writeNewUser(Atm atmEntry) {
        String userId = UUID.randomUUID().toString().replace("-", "");
        User user = new User(
                userId,
                Settings.System.getString(getContentResolver(), "device_name"),
                atmEntry.getId(),
                atmEntry.getRouteTiming()
        );

        mDatabase.child("users").child(userId).setValue(user);
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
}
