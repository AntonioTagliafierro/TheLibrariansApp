package com.example.thelibrariansapp;

import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {


    protected void setupMenuButtons() {
        ImageButton homeButton = findViewById(R.id.ImgBtnHome);
        ImageButton carrelloButton = findViewById(R.id.imgBtnCarrello);
        ImageButton profiloButton = findViewById(R.id.imgBtnProfile);


        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.navigateToHome(NavigationActivity.this);
            }
        });
        carrelloButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.navigateToCarrello(NavigationActivity.this);
            }
        });
        profiloButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationManager.navigateToProfile(NavigationActivity.this);
            }
        });
    }
}