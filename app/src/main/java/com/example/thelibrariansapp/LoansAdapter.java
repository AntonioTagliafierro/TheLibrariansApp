package com.example.thelibrariansapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.models.Loans;

import java.util.List;

public class LoansAdapter extends RecyclerView.Adapter<LoansAdapter.LoanViewHolder> {
    private List<Loans> loansList;
    private Context context;

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
        holder.bookTitle.setText("Titolo libro: " + loan.getBooks().get(0).getTitle());
        holder.startDate.setText("Data inizio: " + loan.getStartDate().toString());
        holder.dueDate.setText("Data fine: " + loan.getDueDate().toString());
        holder.status.setText("Stato: " + loan.getStatus());
    }

    @Override
    public int getItemCount() {
        return loansList.size();
    }

    public static class LoanViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle, startDate, dueDate, status;

        public LoanViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.textViewBookNames);
            startDate = itemView.findViewById(R.id.textViewStartDate);
            dueDate = itemView.findViewById(R.id.textViewEndDate);
            status = itemView.findViewById(R.id.textViewStatus);
        }
    }
}
