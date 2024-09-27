package com.example.thelibrariansapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.models.Book;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Book> results;



    public ResultAdapter(ArrayList<Book> results, Context context) {
        this.context = context;
        this.results = results;
    }

    public Book getItem(int position) {
        return results.get(position);
    }

    public ArrayList<Book> getResults() {
        return results;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.result_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setMovie(results.get(position));


        holder.mostraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BookActivity.class);
                intent.putExtra("Book", results.get(position));
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return results.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView bookImg;
        public TextView bookTitle, bookQuantita, mostraBtn;

        public MyViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            bookQuantita = itemView.findViewById(R.id.bookQuantity);
            bookImg = itemView.findViewById(R.id.bookImg);
            mostraBtn = itemView.findViewById(R.id.showBtn);
        }

        void setMovie(Book book) {
            bookTitle.setText(book.getTitle());
            bookQuantita.setText(book.getQuantity());

            if (book.getImageUrl() == null) {
                bookImg.setImageResource(R.drawable.ic_launcher_background);
            }
            else {
                Picasso.get().load( book.getImageUrl()).into(bookImg);
            }
        }
    }
}