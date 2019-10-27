package com.junction.otpbanking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class AmountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amount);

        EditText etAmount = findViewById(R.id.etAmount);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ListingActivity.class);
            intent.putExtra("take", true);
            intent.putExtra("amount", Double.parseDouble(String.valueOf(etAmount.getText())));
            startActivity(intent);
        });

    }
}
