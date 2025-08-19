package com.s23010738.lifelog;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        EditText nameEdit = findViewById(R.id.name);
        EditText emailEdit = findViewById(R.id.email1);
        EditText passwordEdit = findViewById(R.id.password1);
        EditText confirmPasswordEdit = findViewById(R.id.password2);
        Button btnGetStarted = findViewById(R.id.create_button);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        btnGetStarted.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString();
            String confirmPassword = confirmPasswordEdit.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.checkEmailExists(email)) {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean inserted = dbHelper.insertUser(name, email, password);
            if (inserted) {
                Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Error creating account", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
