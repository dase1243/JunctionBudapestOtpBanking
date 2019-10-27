package com.junction.otpbanking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.junction.otpbanking.flappybird.FlappyBirdMainActivity;

public class FinishTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_trip);

        Button btnPlayGame = findViewById(R.id.btnPlayGame);
        Button btnScanBarcode = findViewById(R.id.btnScanBarcode);
        Button btnStartAgain = findViewById(R.id.btnStartAgain);

        btnPlayGame.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), FlappyBirdMainActivity.class);
            finish();
            startActivity(intent);
        });

        btnScanBarcode.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
            intent.putExtra("take", getIntent().getBooleanExtra("take", false));
            intent.putExtra("put", getIntent().getBooleanExtra("put", false));
            intent.putExtra("amount", getIntent().getDoubleExtra("amount", -1));
            finish();
            startActivity(intent);
        });

        btnStartAgain.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TakePut.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
