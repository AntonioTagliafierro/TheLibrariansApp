package com.example.thelibrariansapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    private ArrayList<Book> bookList; // Memorizza la lista di libri
    private BookAdapter bookAdapter;

    private ImageButton homeButton;
    private ImageButton carrelloButton;
    private ImageButton profiloButton;

    private EditText cercaEditText;
    private Button cercaBtn, filterBtn;
    private Spinner genereSpinner;

    private RecyclerView books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inizializza le view
        homeButton = findViewById(R.id.imgBtnHome);
        carrelloButton = findViewById(R.id.imgBtnCarrello);
        profiloButton = findViewById(R.id.imgBtnProfile);
        cercaEditText = findViewById(R.id.cercaEditText);
        cercaBtn = findViewById(R.id.cercaBtn);
        filterBtn = findViewById(R.id.filterBtn);
        genereSpinner = findViewById(R.id.genereSpinner);
        books = findViewById(R.id.booksRecyclerView);

        // Imposta il LayoutManager per RecyclerView
        books.setLayoutManager(new LinearLayoutManager(this));

        // Gestione della navigazione nei pulsanti
        homeButton.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        carrelloButton.setOnClickListener(v -> startActivity(new Intent(this, CarrelloActivity.class)));
        profiloButton.setOnClickListener(v -> startActivity(new Intent(this, ProfiloActivity.class)));

        // Gestione visibilità filtri e cerca
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

        // Carica i libri dal server
        loadBooksFromServer();

        // Imposta il listener per il pulsante di ricerca
        cercaBtn.setOnClickListener(v -> searchBooks());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carica i libri dal server solo se non sono già stati caricati
        if (bookList == null) {
            loadBooksFromServer();
        } else {
            // Aggiorna l'adapter se i libri sono già caricati
            bookAdapter.notifyDataSetChanged();
        }
    }

    private void loadBooksFromServer() {
        new Thread(() -> {
            SocketClient client = new SocketClient();
            bookList = client.getAllBooks("allbooks"); // Ottieni i libri dal server

            runOnUiThread(() -> {
                if (bookList != null && !bookList.isEmpty()) {
                    bookAdapter = new BookAdapter(bookList, HomeActivity.this);
                    books.setAdapter(bookAdapter);
                } else {
                    Toast.makeText(HomeActivity.this, "Nessun libro trovato", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void searchBooks() {
        String titolo = cercaEditText.getText().toString().trim();
        String genere = genereSpinner.getSelectedItem().toString();

        new Thread(() -> {
            SocketClient client = new SocketClient();
            String type;
            ArrayList<Book> filteredBooks = null;

            if (titolo.isEmpty() && genereSpinner.getSelectedItemPosition() == 0) {
                // Caso 1: EditText vuoto e nessun genere selezionato
                type = "allbooks";
                filteredBooks = client.getFilteredBooks(type);
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Nessun titolo o genere selezionato", Toast.LENGTH_SHORT).show());
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
            runOnUiThread(() -> {
                if (finalFilteredBooks != null && !finalFilteredBooks.isEmpty()) {
                    bookAdapter.updateBooks(finalFilteredBooks);
                } else {
                    Toast.makeText(HomeActivity.this, "Nessun libro trovato per i criteri di ricerca", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
