package com.example.thelibrariansapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;


import com.example.thelibrariansapp.LateLoansDialogFragment;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.utils.SocketClient;

public class ProfiloActivity extends ImmersiveActivity {

    private ImageButton homeButton;
    private ImageButton carrelloButton;
    private ImageButton profiloButton;
    private SharedPreferences sharedPreferences;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profilo);


        // Recupera lo username dalle SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "default_value");
        TextView usernameTextView = findViewById(R.id.usernameText);
        usernameTextView.setText(username);

        Button butt = findViewById(R.id.button);
        butt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfiloActivity.this, ManageBooksActivity.class);
                startActivity(intent);
            }
        });

        checkAvaiable();

        //tasto di uscita
        Button ButtonEsci = findViewById(R.id.esciButton);
        ButtonEsci.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfiloActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        //bottom menu
        ImageButton homeButton = findViewById(R.id.imgBtnHome);
        ImageButton carrelloButton = findViewById(R.id.imgBtnCarrello);
        ImageButton profiloButton = findViewById(R.id.imgBtnProfile);

        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        });

        carrelloButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CarrelloActivity.class);
            startActivity(intent);
        });

        profiloButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfiloActivity.class);
            startActivity(intent);
        });




    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAvaiable();
    }

    private void checkAvaiable() {

        new Thread(() -> {
            SocketClient client = new SocketClient();
            String response = client.check("checkavaiable", username);

            runOnUiThread(() -> {
                if ("Hai dei libri terminati nel carrello".equals(response)) {
                    // Eseguire azione
                    LateLoansDialogFragment dialog = new LateLoansDialogFragment();
                    dialog.show(getSupportFragmentManager(), "NotAvaiableDialog");

                } else {
                    Toast.makeText(ProfiloActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            });

        }).start();

    }
}