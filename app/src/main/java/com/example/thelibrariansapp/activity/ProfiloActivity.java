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



import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.utils.SocketClient;

public class ProfiloActivity extends ImmersiveActivity {

    private ImageButton homeButton;
    private ImageButton carrelloButton;
    private ImageButton profiloButton;
    private SharedPreferences sharedPreferences;
    private String username;
    SocketClient socketClient = new SocketClient();

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

        getNumberUserLoans();
        getMaxPrestiti();


    }
    @Override
    protected void onResume() {
        super.onResume();
        getNumberUserLoans();
        getMaxPrestiti();
    }

    TextView maxLoansTextView = findViewById(R.id.showMaxPrestitiTV);



    private int getMaxPrestiti() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String response = String.valueOf(socketClient.nMaxPrestiti("getmaxprestiti"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        maxLoansTextView.setText(response);
                    }
                });

            }
        }).start();


        return 0;
    }

    TextView LoansAttualiTextView = findViewById(R.id.showNumPrestitiTV);



    private int getNumberUserLoans() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                String response = String.valueOf(socketClient.getNLease("numprestiti", username));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LoansAttualiTextView.setText(response);
                    }
                });

            }
        }).start();


        return 0;
    }

}

