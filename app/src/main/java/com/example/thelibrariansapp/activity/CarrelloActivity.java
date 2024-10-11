package com.example.thelibrariansapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.thelibrariansapp.adapters.BagBooksAdapter;
import com.example.thelibrariansapp.utils.CartManager;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.utils.SocketClient;
import com.example.thelibrariansapp.models.Book;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CarrelloActivity extends ImmersiveActivity {

    private MediaPlayer mediaPlayer; // Per riprodurre suoni
    private ImageButton homeButton, carrelloButton, profiloButton; // Pulsanti del menu
    private RecyclerView books; // RecyclerView per mostrare i libri nel carrello
    private BagBooksAdapter bookAdapter; // Adapter per la RecyclerView
    private Button ordinaBtn; // Pulsante per ordinare i libri
    private ArrayList<Book> bookList; // Lista di libri nel carrello
    private SharedPreferences sharedPreferences; // SharedPreferences per recuperare lo username
    private String username; // Username dell'utente
    private int prestiti = 0; // Numero di prestiti attuali
    private final int max = 10; // Numero massimo di prestiti
    private int sizeCarrello = 0; // Dimensione del carrello

    // Executor per gestire operazioni di rete
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_carrello);

        // Inizializzazione del menu in basso
        homeButton = findViewById(R.id.imgBtnHome);
        carrelloButton = findViewById(R.id.imgBtnCarrello);
        profiloButton = findViewById(R.id.imgBtnProfile);

        // Riproduzione di un suono
        mediaPlayer = MediaPlayer.create(this, R.raw.suono_ordine);

        // Gestione dei clic sui pulsanti del menu
        homeButton.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        carrelloButton.setOnClickListener(v -> startActivity(new Intent(this, CarrelloActivity.class)));
        profiloButton.setOnClickListener(v -> startActivity(new Intent(this, ProfiloActivity.class)));

        // Inizializzazione SharedPreferences per recuperare lo username
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", "default_value");

        // Inizializza la RecyclerView
        books = findViewById(R.id.recyclerViewLoans);
        books.setLayoutManager(new LinearLayoutManager(this));

        // Inizializza la lista di libri e carica i libri nel carrello
        bookList = new ArrayList<>(CartManager.getInstance().getBookList());
        sizeCarrello = bookList.size();
        bookAdapter = new BagBooksAdapter(bookList, CarrelloActivity.this);
        books.setAdapter(bookAdapter);

        ordinaBtn = findViewById(R.id.orderBtn);

        // Thread per ottenere il numero di prestiti attuali dell'utente
        executorService.execute(() -> {
            SocketClient client = new SocketClient();
            prestiti = client.getNLease("numprestiti", username);
        });

        // Thread per ottenere i libri salvati nel carrello
        executorService.execute(() -> {
            SocketClient client = new SocketClient();
            ArrayList<Book> serverBooks = client.getBagBooks("bagbooks", username, this);

            // Aggiorna l'interfaccia utente dopo aver ottenuto i libri
            runOnUiThread(() -> {
                if (serverBooks != null && !serverBooks.isEmpty()) {
                    bookList.clear();
                    bookList.addAll(serverBooks);
                    sizeCarrello = bookList.size();
                    bookAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CarrelloActivity.this, "Carrello vuoto", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Controlla i libri non più disponibili
        avvisaDisponibile(bookList);

        // Gestione del clic sul pulsante di ordinazione
        ordinaBtn.setOnClickListener(v -> {
            if (prestiti + sizeCarrello <= max) {
                executorService.execute(() -> {
                    ArrayList<Book> booksToRemove = new ArrayList<>();

                    // Per ogni libro nel carrello, invia un ordine
                    for (Book book : bookList) {
                        if (book.getAvailable() > 0) {
                            SocketClient client = new SocketClient();
                            String response = client.bookTODB("ordina", username, book.getIsbn());

                            // Gestisci la risposta nel thread principale
                            runOnUiThread(() -> {
                                if ("Ordine avvenuto con successo!".equals(response)) {
                                    Toast.makeText(CarrelloActivity.this, "Ordine confermato per il libro: " + book.getIsbn(), Toast.LENGTH_SHORT).show();
                                    prestiti++;
                                    booksToRemove.add(book); // Aggiungi il libro ordinato alla lista
                                    if (mediaPlayer != null) {
                                        mediaPlayer.start();
                                    }
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

                    // Rimuovi i libri confermati dalla lista principale e aggiorna l'adapter
                    runOnUiThread(() -> {
                        bookList.removeAll(booksToRemove);
                        bookAdapter.updateBooks(bookList);
                    });
                });
            } else {
                // Mostra un messaggio di errore se il numero di prestiti supera il massimo consentito
                Toast.makeText(CarrelloActivity.this, "Numero prestiti consentito superato: hai " + prestiti +
                        " prestiti attivi e nel carrello " + sizeCarrello + " libri, mentre il massimo è " + max, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void avvisaDisponibile(ArrayList<Book> bookList) {
        for (Book book : bookList) {
            if (book.getAvailable() < 1) {
                Toast.makeText(CarrelloActivity.this, "Il libro " + book.getTitle() + " non è più disponibile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Rilascia il MediaPlayer quando l'attività viene distrutta
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        // Shutdown dell'executor service
        executorService.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ricarica la lista dei libri dal server o dal database
        reloadBooks();
    }

    private void reloadBooks() {
        executorService.execute(() -> {
            SocketClient client = new SocketClient();
            ArrayList<Book> serverBooks = client.getBagBooks("bagbooks", username, this);

            // Aggiorna l'interfaccia utente dopo aver ottenuto i libri
            runOnUiThread(() -> {
                if (serverBooks != null && !serverBooks.isEmpty()) {
                    bookList.clear();
                    bookList.addAll(serverBooks);
                    sizeCarrello = bookList.size();
                    bookAdapter.notifyDataSetChanged();
                } else {
                    bookList.clear();
                    sizeCarrello = 0;
                    bookAdapter.notifyDataSetChanged();
                    Toast.makeText(CarrelloActivity.this, "Carrello vuoto", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
