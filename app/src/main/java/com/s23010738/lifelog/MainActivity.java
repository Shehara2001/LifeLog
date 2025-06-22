package com.s23010738.lifelog;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button getStartedBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStartedBtn = findViewById(R.id.get_started_button);
        getStartedBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PlannerActivity.class);
            startActivity(intent);
        });

    }
}
