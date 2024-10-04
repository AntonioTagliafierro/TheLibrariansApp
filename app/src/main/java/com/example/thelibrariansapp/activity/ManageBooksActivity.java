package com.example.thelibrariansapp.activity;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.adapters.LoansAdapter;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.models.Book;
import com.example.thelibrariansapp.utils.SocketClient;
import com.example.thelibrariansapp.models.Loans;

import java.util.ArrayList;
import java.util.List;

public class ManageBooksActivity extends ImmersiveActivity {

    private RecyclerView loansRecyclerView;
    private LoansAdapter loansAdapter;
    private List<Loans> loansList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private String username;
    private SocketClient socketClient = new SocketClient();

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
        loadLoans();

    }
        private void loadLoans (){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SocketClient client = new SocketClient();
                    loansList = client.getUserLoans("userloans", username);  // Passa il tipo di prestito e l'username

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
