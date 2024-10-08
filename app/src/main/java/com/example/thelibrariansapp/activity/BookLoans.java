package com.example.thelibrariansapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.adapters.BookAdapter;
import com.example.thelibrariansapp.adapters.RecommendedLoanAdapter;
import com.example.thelibrariansapp.models.Book;
import com.example.thelibrariansapp.models.Loans;
import com.example.thelibrariansapp.utils.SocketClient;

import java.util.ArrayList;
import java.util.List;


public class BookLoans extends ImmersiveActivity {

    private TextView textViewTitle, textViewAuthor,textViewCategory,textViewTotalCopies,
    textViewCopiesInUse,textViewISBN;
    private ImageView ImageViewCopertinaLibro;
    private Book object;

    private ArrayList<Loans> ItemsLoans = new ArrayList<>();
    private ArrayList<Loans> itemsLoansRitardo = new ArrayList<>();

    SocketClient client = new SocketClient();

    private RecyclerView.Adapter adapterRecommended,adapterRecommendedRitardo;
    private  RecyclerView recyclerViewLoans,recyclerViewLoansRitardo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_loans);



        //Navigate to Homepage
        ImageButton BackHomepageButton = findViewById(R.id.imageButtonBack);
        BackHomepageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookLoans.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Information of the book
        initView();
        getBundle();

        new Thread(new Runnable() {
            @Override
            public void run() {
                SocketClient client = new SocketClient();

                // Ottieni la lista di libri dal server
                ItemsLoans = client.getBookLoans("loansbyisbnnotdelivered",object.getIsbn());
                itemsLoansRitardo = client.getBookLoans("overdueloansbyisbn",object.getIsbn());

                // Aggiorna l'interfaccia utente
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initRecyclerviewRitardo(itemsLoansRitardo);
                        if (ItemsLoans != null && !ItemsLoans.isEmpty()) {
                            //Add element in the recycler view

                            initRecyclerview(ItemsLoans);
                        } else {
                            Toast.makeText(BookLoans.this, "Non ci sono prestiti attivi per questo libro", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();




        }



    private void initRecyclerview(ArrayList<Loans> itemsLoans) {
        //Add element in the recycler view
        recyclerViewLoans = findViewById(R.id.recyclerViewLoans);
        recyclerViewLoans.setHasFixedSize(true);
        recyclerViewLoans.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterRecommended = new RecommendedLoanAdapter(itemsLoans);
        recyclerViewLoans.setAdapter(adapterRecommended);

    }

    private void initRecyclerviewRitardo(ArrayList<Loans> itemsLoansRitardo) {
        //Add element in the recycler view
        recyclerViewLoansRitardo = findViewById(R.id.recyclerViewLoansRitardo);
        recyclerViewLoansRitardo.setHasFixedSize(true);
        recyclerViewLoansRitardo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterRecommendedRitardo = new RecommendedLoanAdapter(itemsLoansRitardo);
        recyclerViewLoansRitardo.setAdapter(adapterRecommendedRitardo);

    }

    @SuppressLint("SuspiciousIndentation")
    private void getBundle() {
            object = (Book) getIntent().getSerializableExtra("object");
        int quantity = object.getQuantity();
        System.out.println("la quantita è " + object.getQuantity());
        Log.d("BookLoans", "Quantity: " + quantity);
            Glide.with(this).load(object.getImageUrl()).centerInside().into(ImageViewCopertinaLibro);

            textViewTitle.setText(object.getTitle());
            textViewAuthor.setText(object.getAuthor());
            textViewCategory.setText(object.getGenre());
            textViewISBN.setText(object.getIsbn());
            textViewTotalCopies.setText(String.valueOf(object.getQuantity()));
            textViewCopiesInUse.setText(String.valueOf(object.getCopyOnLease()));

        }

    private void initView() {
        textViewTitle = findViewById(R.id.textViewTitolo);
        textViewAuthor = findViewById(R.id.textViewAuthor);
        textViewCategory = findViewById(R.id.textViewCategoryInformation);
        textViewISBN = findViewById(R.id.textViewISBN);
        textViewTotalCopies = findViewById(R.id.textViewTotalCopies);
        textViewCopiesInUse = findViewById(R.id.textViewCopiesInUse);
        ImageViewCopertinaLibro = findViewById(R.id.imageViewCopertinaLibro);

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Esegui un nuovo fetch dei dati quando l'attività riprende
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Ricarica i prestiti dal server
                itemsLoansRitardo = client.getBookLoans("overdueloansbyisbn", object.getIsbn());

                // Aggiorna la RecyclerView nell'interfaccia utente
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Ricarica la lista dei prestiti in ritardo
                        initRecyclerviewRitardo(itemsLoansRitardo);
                    }
                });
            }
        }).start();
    }


}
