package com.s23010738.lifelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Button btnGetStarted = findViewById(R.id.create_button);
        btnGetStarted.setOnClickListener(v ->
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class))
        );
    }
}
