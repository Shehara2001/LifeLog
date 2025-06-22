package com.s23010738.lifelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class PlannerActivity extends AppCompatActivity {
    Button nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);

        nextBtn = findViewById(R.id.next_button);

        nextBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PlannerActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}

