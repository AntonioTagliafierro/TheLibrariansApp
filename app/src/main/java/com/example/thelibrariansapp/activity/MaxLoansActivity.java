package com.example.thelibrariansapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;

import com.example.thelibrariansapp.R;

public class MaxLoansActivity extends ImmersiveActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.acitivity_max_loans);

    }


    }
