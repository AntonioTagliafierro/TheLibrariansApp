package com.example.thelibrariansapp.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.adapters.BookAdapter;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.utils.SocketClient;
import com.example.thelibrariansapp.models.Book;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {


    private EditText cercaEditText;
    private Button cercaBtn, filterBtn;
    private Spinner genereSpinner;

    private RecyclerView books; // Assicurati che questo sia il nome corretto per il tuo RecyclerView
    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inizializza le view

        cercaEditText = findViewById(R.id.cercaEditText);
        cercaBtn = findViewById(R.id.cercaBtn);
        filterBtn = findViewById(R.id.filterBtn);
        genereSpinner = findViewById(R.id.genereSpinner);

        //gestione visibilità filtri e cerca
        filterBtn.setOnClickListener(new View.OnClickListener() {
            boolean isVisible = false;

            @Override
            public void onClick(View v) {
                if (isVisible) {
                    cercaBtn.setVisibility(View.GONE);
                    cercaEditText.setVisibility(View.GONE);
                    genereSpinner.setVisibility(View.GONE);
                } else {
                    cercaBtn.setVisibility(View.VISIBLE);
                    cercaEditText.setVisibility(View.VISIBLE);
                    genereSpinner.setVisibility(View.VISIBLE);
                }
                isVisible = !isVisible;
            }
        });

        books = findViewById(R.id.booksRecyclerView); // Assicurati che questo ID corrisponda al tuo layout XML

        // Imposta il LayoutManager per RecyclerView
        books.setLayoutManager(new LinearLayoutManager(this));

        // Thread per ottenere i libri dal server
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketClient client = new SocketClient();

                // Ottieni la lista di libri dal server
                ArrayList<Book> bookList = client.getAllBooks("allbooks");

                // Aggiorna l'interfaccia utente
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bookList != null && !bookList.isEmpty()) {
                            bookAdapter = new BookAdapter(bookList, HomeActivity.this);

                            books.setAdapter(bookAdapter);
                        } else {
                            Toast.makeText(HomeActivity.this, "Nessun libro trovato", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();


        // Imposta il listener per il pulsante di ricerca
        cercaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titolo = cercaEditText.getText().toString().trim();
                String genere = genereSpinner.getSelectedItem().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SocketClient client = new SocketClient();
                        String type;
                        ArrayList<Book> filteredBooks = null;
                        if (titolo.isEmpty() && genereSpinner.getSelectedItemPosition() == 0) {
                            // Caso 1: EditText vuoto e nessun genere selezionato
                            type = "allbooks";
                            filteredBooks = client.getFilteredBooks(type);
                            Toast.makeText(getApplicationContext(), "Nessun titolo o genere selezionato", Toast.LENGTH_SHORT).show();
                        } else if (!titolo.isEmpty() && genereSpinner.getSelectedItemPosition() != 0) {
                            // Caso 2: Titolo presente e genere selezionato
                            type = "totalfilter:" + titolo + ":" + genere;
                            filteredBooks = client.getFilteredBooks(type);

                        } else if (!titolo.isEmpty() && genereSpinner.getSelectedItemPosition() == 0) {
                            // Caso 3: Titolo presente, ma nessun genere selezionato
                            type = "onlytitle:" + titolo;
                            filteredBooks = client.getFilteredBooks(type);

                        } else if (titolo.isEmpty() && genereSpinner.getSelectedItemPosition() != 0) {
                            // Caso 4: Nessun titolo ma genere selezionato
                            type = "onlygenre:" + genere;
                            filteredBooks = client.getFilteredBooks(type);
                        }


                        ArrayList<Book> finalFilteredBooks = filteredBooks;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalFilteredBooks != null && !finalFilteredBooks.isEmpty()) {
                                    bookAdapter.updateBooks(finalFilteredBooks);
                                } else {
                                    Toast.makeText(HomeActivity.this, "Nessun libro trovato per i criteri di ricerca", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
            }
        });



    }

    }

