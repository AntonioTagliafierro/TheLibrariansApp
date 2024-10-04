package com.example.thelibrariansapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;



import com.example.thelibrariansapp.R;

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

        ImageButton ButtonBack = findViewById(R.id.imageButtonBack);
        ButtonBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfiloActivity.this, HomeActivity.class);
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
}