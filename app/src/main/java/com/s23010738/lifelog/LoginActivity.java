package com.s23010738.lifelog;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText email, password;
    Button signInBtn, createAccountBtn;
    TextView forgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide system navigation bar for immersive fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signInBtn = findViewById(R.id.signin_button);
        createAccountBtn = findViewById(R.id.create_account_button);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        signInBtn.setOnClickListener(v -> {
            String emailVal = email.getText().toString();
            String passVal = password.getText().toString();
            if (emailVal.isEmpty() || passVal.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.checkUser(emailVal, passVal)) {
                Toast.makeText(this, "Signing in as " + emailVal, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(view -> {
            // Create an intent to navigate to CreateAccountActivity
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

       createAccountBtn.setOnClickListener(view -> {
            // Create an intent to navigate to CreateAccountActivity
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }
}
