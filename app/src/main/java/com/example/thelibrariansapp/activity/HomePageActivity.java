package com.example.thelibrariansapp.activity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.adapters.BookAdapter;
import com.example.thelibrariansapp.adapters.RecommendedBookAdapter;
import com.example.thelibrariansapp.utils.SocketClient;
import com.example.thelibrariansapp.models.Book;

import java.util.ArrayList;
public class  HomePageActivity extends AppCompatActivity {

    private RecyclerView.Adapter adapterRecommended;
    private  RecyclerView recyclerViewBooks;
    private ImageButton exit;

    private EditText searchbar;

    private ArrayList<Book> ItemsBooks = new ArrayList<>();

    private SocketClient socketClient = new SocketClient();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminhomepage);

        // Thread per ottenere i libri dal server
        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketClient client = new SocketClient();

                // Ottieni la lista di libri dal server
                ArrayList<Book> ItemsBooks = socketClient.getAllBooks("allbooks");

                // Aggiorna l'interfaccia utente
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ItemsBooks != null && !ItemsBooks.isEmpty()) {
                            //Add element in the recycler view
                            initRecyclerview(ItemsBooks);
                        } else {
                            Toast.makeText(HomePageActivity.this, "Nessun libro trovato", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();



        //Exit button
        exit = findViewById(R.id.Exit_button);
        exit.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
            builder.setTitle("Attenzione");
            builder.setMessage("Sei sicuro di voler uscire?");
            builder.setPositiveButton("Si", (dialog, which) -> {
                Intent intent = new Intent(HomePageActivity.this, LoginActivityAdmin.class);
                startActivity(intent);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        //Search bar
        searchbar = findViewById(R.id.Searchbar);
        // Detect touch on drawableEnd (the clear icon)
        searchbar.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                // Check if the touch was on the drawableEnd (right side of the EditText)
                if (event.getRawX() >= (searchbar.getRight() - searchbar.getCompoundDrawables()[2].getBounds().width())) {
                    // Clear the text
                    searchbar.setText("");
                    return true;
                }

                //Check if the touch was on the drawableStart
                if (event.getRawX() <= (searchbar.getLeft() + searchbar.getCompoundDrawables()[0].getBounds().width())) {
                    ArrayList<Book> BookIsbn = socketClient.getBooksByIsbn(searchbar.getText().toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SocketClient client = new SocketClient();

                            // Ottieni la lista di libri dal server
                            ArrayList<Book> BookIsbn = socketClient.getBooksByIsbn(searchbar.getText().toString());

                            // Aggiorna l'interfaccia utente
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (BookIsbn.size() > 0) {
                                        Intent intent = new Intent(HomePageActivity.this, BookLoans.class);
                                        intent.putExtra("object", BookIsbn.get(0));
                                        startActivity(intent);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(HomePageActivity.this);
                                        builder.setTitle("Attenzione");
                                        builder.setMessage("Nessun libro trovato con questo ISBN");
                                        builder.setPositiveButton("Ok", (dialog, which) -> searchbar.setText(""));
                                        builder.show();
                                    }
                                }
                            });
                        }
                    }).start();

                    return true;
                }


            }
            return false;
        });




    }

    private void initRecyclerview(ArrayList<Book> itemsBooks) {
        recyclerViewBooks= findViewById(R.id.recyclerViewBooks);
        recyclerViewBooks.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        adapterRecommended = new RecommendedBookAdapter(itemsBooks);
        recyclerViewBooks.setAdapter(adapterRecommended);
    }
}
