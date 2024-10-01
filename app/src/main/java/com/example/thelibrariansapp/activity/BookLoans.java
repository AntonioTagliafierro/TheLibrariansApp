package com.example.thelibrariansapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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


public class BookLoans extends AppCompatActivity {

    private TextView textViewTitle, textViewAuthor,textViewCategory,textViewTotalCopies,
    textViewCopiesInUse,textViewISBN;
    private ImageView ImageViewCopertinaLibro;
    private Book object;


    SocketClient client = new SocketClient();

    private RecyclerView.Adapter adapterRecommended;
    private  RecyclerView recyclerViewLoans;

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
                ArrayList<Loans> ItemsLoans = client.getBookLoans("loansbyisbn",object.getIsbn());
                System.out.println(ItemsLoans.get(0).getUser().getUsername());
                System.out.println(ItemsLoans.get(0).getStatus());
                // Aggiorna l'interfaccia utente
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (ItemsLoans != null && !ItemsLoans.isEmpty()) {
                            //Add element in the recycler view

                            initRecyclerview(ItemsLoans);
                        } else {
                            Toast.makeText(BookLoans.this, "Nessun libro trovato", Toast.LENGTH_SHORT).show();
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

    @SuppressLint("SuspiciousIndentation")
    private void getBundle() {
            object = (Book) getIntent().getSerializableExtra("object");
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


}
