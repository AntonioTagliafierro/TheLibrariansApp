package com.example.thelibrariansapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.models.Loans;
import com.example.thelibrariansapp.utils.SocketClient;

import java.util.List;

public class LoansAdapter extends RecyclerView.Adapter<LoansAdapter.LoanViewHolder> {

    private List<Loans> loansList;
    private Context context;
    private SocketClient socketClient = new SocketClient();
    private ImageView bookCover;

    public LoansAdapter(List<Loans> loansList, Context context) {
        this.loansList = loansList;
        this.context = context;
    }

    @Override
    public LoanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lendlease, parent, false);
        return new LoanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LoanViewHolder holder, int position) {
        Loans loan = loansList.get(position);

        // Popola i dati del prestito nel ViewHolder
        holder.bookTitle.setText("Titolo libro: " + loan.getBook().getTitle());
        holder.startDate.setText("Data inizio: " + loan.getStartDate().toString());
        holder.dueDate.setText("Data fine: " + loan.getDueDate().toString());
        holder.status.setText("Stato: " + loan.getStatus());
        holder.isbn.setText("ISBN: " + loan.getBook().getIsbn());
        holder.genre.setText("Genere: " + loan.getBook().getGenre());

        // Caricamento dell'immagine del libro
        Glide.with(context)
                .load(loan.getBook().getImageUrl())
                .centerInside()
                .into(bookCover);
    }

    @Override
    public int getItemCount() {
        return loansList.size();
    }

    public static class LoanViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle, startDate, dueDate, status, isbn, genre;
        ImageView bookImage;

        public LoanViewHolder(View itemView) {
            super(itemView);

            // Inizializza le view con i rispettivi ID
            bookTitle = itemView.findViewById(R.id.textViewBookNames);
            startDate = itemView.findViewById(R.id.textViewStartDate);
            dueDate = itemView.findViewById(R.id.textViewEndDate);
            status = itemView.findViewById(R.id.textViewStatus);
            isbn = itemView.findViewById(R.id.textViewISBN);
            genre = itemView.findViewById(R.id.textViewGenre);
            bookImage = itemView.findViewById(R.id.imageViewBookCover);
        }
    }
}
