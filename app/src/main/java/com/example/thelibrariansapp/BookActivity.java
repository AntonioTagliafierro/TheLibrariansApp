package com.example.thelibrariansapp;



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.thelibrariansapp.models.Book;
import com.example.thelibrariansapp.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private Button aggiungiBtn;
    private ImageView bookCover;
    private TextView isbnBook,bookTitolo, bookGenre, bookAuthor, bookQuantita;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Inizializza le view
        backBtn = findViewById(R.id.backBtn);
        bookCover = findViewById(R.id.bookCover);
        bookTitolo = findViewById(R.id.bookTitle);
        bookGenre = findViewById(R.id.bookGenre);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookQuantita = findViewById(R.id.bookQuantity);
        isbnBook = findViewById(R.id.isbnBook);



        // Imposta il listener per il pulsante di ritorno
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        // Ottiene il libro dalla view precedente
        Intent intent = getIntent();
        Book book = (Book) intent.getSerializableExtra("Book");

        String url = book.getImageUrl();
        System.out.println("Url immagine....");
        System.out.println(url);
        Glide.with(this).load(url).transform(new GranularRoundedCorners(30,30,0,0)).into(bookCover);
        // Imposta i dettagli del libro nelle TextView
        bookTitolo.setText(book.getTitle());
        bookGenre.setText(book.getGenre());
        bookAuthor.setText(book.getAuthor());
        bookQuantita.setText(String.valueOf(book.getAvailable()));

        if (book.getAvailable() < 1){
            aggiungiBtn.setActivated(false);
            aggiungiBtn.setText("Non disponibile");
        } else {
            aggiungiBtn.setActivated(true);
            aggiungiBtn.setText("Aggiungi al carrello");
        }
        // In un'Activity o Fragment dove visualizzi un singolo libro
        Button aggiungiBtn = findViewById(R.id.aggiungiBtn);

        // Inizializzazione SharedPreferences per recuperare lo username
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "default_value");

        aggiungiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Book book = (Book) getIntent().getSerializableExtra("selectedBook");
                // Ottieni il libro corrente
                CartManager.getInstance().addBook(book); // Aggiungi il libro al carrello

                // Thread per aggiungere libro al  DB
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SocketClient client = new SocketClient();
                        String response = client.bookTODB("aggiungialcarrello", username, book.getIsbn());
                        Toast.makeText(BookActivity.this, response, Toast.LENGTH_SHORT).show();

                    }
                }).start();
            }
        });



    }
}