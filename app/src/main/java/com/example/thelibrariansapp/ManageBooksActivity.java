package com.example.thelibrariansapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.models.Loans;

import java.util.ArrayList;
import java.util.List;

public class ManageBooksActivity extends AppCompatActivity {

    private RecyclerView loansRecyclerView;
    private LoansAdapter loansAdapter;
    private List<Loans> loansList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_books);

        // Inizializza la RecyclerView
        loansRecyclerView = findViewById(R.id.recyclerViewLoans);
        loansRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Recupera lo username dalle SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "default_value");

        // Carica i prestiti dell'utente
        loadLoans("active");  // Puoi cambiare il tipo ("active", "completed", ecc.)
    }

    private void loadLoans(String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketClient client = new SocketClient();
                loansList = client.getUserLoans(type, username);  // Passa il tipo di prestito e l'username

                // Aggiorna l'interfaccia utente nel thread principale
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loansAdapter = new LoansAdapter(loansList, ManageBooksActivity.this);
                        loansRecyclerView.setAdapter(loansAdapter);
                    }
                });
            }
        }).start();
    }


}