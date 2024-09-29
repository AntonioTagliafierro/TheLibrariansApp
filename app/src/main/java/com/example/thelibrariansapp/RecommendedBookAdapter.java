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


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import java.util.ArrayList;



public class RecommendedBookAdapter extends RecyclerView.Adapter<RecommendedBookAdapter.Viewholder> {

    ArrayList<CardBookPropertyDomain> items;
    Context context;

    public RecommendedBookAdapter(ArrayList<CardBookPropertyDomain> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_card_book,parent,false);
        context = parent.getContext();
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, @SuppressLint("RecyclerView") int position) {
        holder.TextCategory.setText(items.get(position).getCategory());
        holder.TextTitle.setText(items.get(position).getTitle());
        holder.TextAuthor.setText(items.get(position).getAuthor());
        holder.TextISBN.setText(items.get(position).getISBN());


        int drawableResourceId = holder.itemView.getResources().getIdentifier(items.get(position).getImageUrl(),
                "drawable",holder.itemView.getContext().getPackageName());
        Glide.with(holder.itemView.getContext()).load(drawableResourceId).transform(new GranularRoundedCorners(30,30,0,0)).into(holder.ImageCopertina);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), BookLoans.class);
                intent.putExtra("object",items.get(position));
                holder.itemView.getContext().startActivity(intent);

            }
        });


    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView    TextCategory,TextTitle,TextAuthor,TextISBN;
        ImageView ImageCopertina;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            TextCategory = itemView.findViewById(R.id.textViewCategory);
            TextTitle = itemView.findViewById(R.id.textViewtitle);
            TextAuthor = itemView.findViewById(R.id.textViewAuthorCardBook);
            TextISBN = itemView.findViewById(R.id.textViewISBNCardBook);
            ImageCopertina = itemView.findViewById(R.id.imageViewCopertinaLibroCardBook);
        }
    }
}
