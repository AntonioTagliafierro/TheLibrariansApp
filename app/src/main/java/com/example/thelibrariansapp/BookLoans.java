package com.example.thelibrariansapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thelibrariansapp.models.Book;

import java.time.LocalDate;
import java.util.ArrayList;


public class BookLoans extends AppCompatActivity {

    private TextView textViewTitle, textViewAuthor,textViewCategory,textViewTotalCopies,
    textViewCopiesInUse,textViewISBN;
    private ImageView ImageViewCopertinaLibro;
    private Book object;

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

        //Add element in the recycler view
        initRecyclerview();
        }

    private void initRecyclerview() {
        //Add element in the recycler view
        ArrayList<CardLoanPropertyDomain> itemsLoans = new ArrayList<>();
        itemsLoans.add(new CardLoanPropertyDomain("alessandro@gmail.com", LocalDate.parse("2024-09-09"), LocalDate.parse("2024-09-16")));
        itemsLoans.add(new CardLoanPropertyDomain("marco@gmail.com", LocalDate.parse("2024-09-09"), LocalDate.parse("2024-09-24")));
        itemsLoans.add(new CardLoanPropertyDomain("giovanni@gmail.com", LocalDate.parse("2024-09-09"), LocalDate.parse("2024-09-20")));
        itemsLoans.add(new CardLoanPropertyDomain("luca@gmail.com", LocalDate.parse("2024-09-09"), LocalDate.parse("2024-09-25")));
        itemsLoans.add(new CardLoanPropertyDomain("antonio@gmail.com", LocalDate.parse("2024-09-09"), LocalDate.parse("2024-09-29")));
        itemsLoans.add(new CardLoanPropertyDomain("giovanniesposito2022@gmail.com", LocalDate.parse("2024-09-09"), LocalDate.parse("2024-09-29")));


        recyclerViewLoans = findViewById(R.id.recyclerViewLoans);
        recyclerViewLoans.setHasFixedSize(true);
        recyclerViewLoans.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterRecommended = new RecommendedLoanAdapter(itemsLoans);
        recyclerViewLoans.setAdapter(adapterRecommended);

    }

    private void getBundle() {
        object = (Book) getIntent().getSerializableExtra("object");
        int drawableResourceId = this.getResources().getIdentifier(object.getImageUrl(), "drawable", this.getPackageName());
            Glide.with(this).load(drawableResourceId).into(ImageViewCopertinaLibro);

            textViewTitle.setText(object.getTitle());
            textViewAuthor.setText(object.getAuthor());
            textViewCategory.setText(object.getGenre());
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
