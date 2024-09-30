package com.example.thelibrariansapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.models.Book;

import java.util.ArrayList;

public class CarrelloActivity extends AppCompatActivity {

    private RecyclerView books; // RecyclerView per mostrare i libri nel carrello
    private BagBooksAdapter bookAdapter; // Adapter per la RecyclerView
    private Button ordinaBtn; // Bottone per ordinare i libri
    ArrayList<Book> bookList; // Lista di libri nel carrello
    private SharedPreferences sharedPreferences; // SharedPreferences per recuperare lo username
    private String username; // Username dell'utente
    private int prestiti = 0; // Numero di prestiti attuali
    private final int max = 10; // Numero massimo di prestiti
    private int sizeCarrello = 0; // Dimensione del carrello

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrello);

        // Inizializzazione SharedPreferences per recuperare lo username
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "default_value");

        bookList = CartManager.getInstance().getBookList();
        sizeCarrello = bookList.size();

        bookAdapter = new BagBooksAdapter(bookList, CarrelloActivity.this);
        books.setAdapter(bookAdapter);
        ordinaBtn = findViewById(R.id.orderBtn); // Inizializza il bottone ordina
        books = findViewById(R.id.booksRecyclerView); // Inizializza la RecyclerView

        // Imposta il LayoutManager per la RecyclerView
        books.setLayoutManager(new LinearLayoutManager(this));

        // Thread per ottenere il numero di prestiti attuali dell'utente
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketClient client = new SocketClient();
                prestiti = client.getNLease("numprestiti", username); // Ottieni il numero di prestiti
            }
        }).start();

        // Thread per ottenere i libri salvati nel carrello
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketClient client = new SocketClient();
                ArrayList<Book> serverBooks = client.getBagBooks("bagbooks", username); // Ottieni i libri nel carrello

                // Aggiorna l'interfaccia utente dopo aver ottenuto i libri
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (bookList != null && !bookList.isEmpty()) {
                            bookList.addAll(serverBooks);
                            sizeCarrello = bookList.size();

                            // Inizializza l'adapter
                            bookAdapter.updateBooks(bookList);
                        } else {
                            Toast.makeText(CarrelloActivity.this, "Carrello vuoto", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
//Controlla libri non più disponibili
        avvisaDisponibile(bookList);

// Aggiungi un listener per il bottone ordina
        ordinaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prestiti + sizeCarrello <= max) { // Controlla se i prestiti non superano il massimo
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SocketClient client = new SocketClient();

                            ArrayList<Book> booksToRemove = new ArrayList<>(); // Lista temporanea per i libri da rimuovere

                            // Per ogni libro nel carrello, invia un ordine
                            for (Book book : bookList) {
                                if (book.getAvailable() > 0) {
                                    String response = client.bookTODB("ordina", username, book.getIsbn());

                                    // Gestisci la risposta nel thread principale
                                    runOnUiThread(() -> {
                                        if ("Ordine avvenuto con successo!".equals(response)) {
                                            Toast.makeText(CarrelloActivity.this, "Ordine confermato per il libro: " + book.getIsbn(), Toast.LENGTH_SHORT).show();
                                            prestiti++;

                                            // Aggiungi il libro alla lista dei libri da rimuovere
                                            booksToRemove.add(book);

                                        } else {
                                            Toast.makeText(CarrelloActivity.this, "Errore per il libro: " + book.getIsbn() + " - " + response, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        Toast.makeText(CarrelloActivity.this, "Il libro: " + book.getTitle() + " è terminato", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }

                            // Rimuovi i libri confermati dalla lista principale
                            runOnUiThread(() -> {
                                bookList.removeAll(booksToRemove); // Rimuovi tutti i libri ordinati con successo
                                bookAdapter.updateBooks(bookList); // Notifica l'adapter dell'aggiornamento (già include notifyDataSetChanged())
                            });
                        }
                    }).start();
                } else {
                    // Mostra un messaggio di errore se il numero di prestiti supera il massimo consentito
                    Toast.makeText(CarrelloActivity.this, "Numero prestiti consentito superato: hai " + prestiti +
                                    " prestiti attivi e nel carrello " + sizeCarrello + " libri, mentre il massimo è " + max,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void avvisaDisponibile(ArrayList<Book> bookList){
        for (Book book : bookList) {
            if (book.getAvailable() < 1){
                Toast.makeText(CarrelloActivity.this, "il libro" + book.getTitle() + " non è più disponibile",Toast.LENGTH_SHORT);
            }
        }
    }
}
