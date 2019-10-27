package com.junction.otpbanking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class QuestionActivity extends AppCompatActivity {
    private Button btn_Submit;
    private EditText etSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        btn_Submit = findViewById(R.id.btn_Submit);
        etSpeed = findViewById(R.id.etSpeed);

        btn_Submit.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("speed", etSpeed.getText());
            startActivity(intent);
        });
    }
}
