package com.example.thelibrariansapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivityAdmin extends AppCompatActivity {
EditText email,password;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpageadmin);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button loginButton = findViewById(R.id.loginBtnadmin);
        email = findViewById(R.id.usernameadmin);
        password = findViewById(R.id.passwordadmin);

        //Navigate to HomePageActivity

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(LoginActivityAdmin.this, HomePageActivity.class);
                //Control of email and correct format with a required of domain
                if(email.getText().toString().isEmpty()){
                    email.setError("Email is required");
                    email.requestFocus();
                    return;
                } else if(password.getText().toString().isEmpty()){     //Control of password and correct format with a required of maiusc,minusc and number
                    password.setError("Password is required");
                    password.requestFocus();
                    return;
                } else if(!email.getText().toString().contains("admin@admin.com")|| !password.getText().toString().contains("admin")){
                    new AlertDialog.Builder(LoginActivityAdmin.this)
                            .setTitle("ERROR")
                            .setMessage("UNAUTHORIZED USER, ENTER CORRECT CREDENTIALS!!!!")
                            .setPositiveButton("Ok", (dialog, which) -> {
                                email.setText("");
                                password.setText("");
                            })
                            .show();
                }else
                {

                    startActivity(intent);
                    finish();

                }




       }

            });
    }
}
