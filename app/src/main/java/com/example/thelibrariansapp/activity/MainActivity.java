package com.example.thelibrariansapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.utils.SocketClient;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private TextView registratiLbl;
    private Button loginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registratiLbl = findViewById(R.id.registratiLbl);


        // Imposta il listener per il clic su "Registrati"
        registratiLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.apply();

                // Invia le credenziali al server
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SocketClient client = new SocketClient();
                        String response = client.sendCredentials("login", username, password);

                        // Gestisci la risposta nel thread principale
                        runOnUiThread(() -> {
                            if ("Login avvenuto con successo!".equals(response)) {
                                // Naviga a HomeActivity
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Gestisci l'errore
                                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }
}
