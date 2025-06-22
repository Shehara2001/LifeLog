package com.s23010738.lifelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class VerifyPassword extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Button btnGetStarted = findViewById(R.id.VerifyBtn);
        btnGetStarted.setOnClickListener(v ->
                startActivity(new Intent(VerifyPassword.this, CreateNewPassword.class))
        );
    }
}