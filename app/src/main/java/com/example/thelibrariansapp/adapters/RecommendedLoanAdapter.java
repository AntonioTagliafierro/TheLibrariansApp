package com.example.thelibrariansapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thelibrariansapp.models.CardLoanPropertyDomain;
import com.example.thelibrariansapp.R;
import com.example.thelibrariansapp.models.Loans;

import java.util.ArrayList;
import java.util.List;


public class RecommendedLoanAdapter extends RecyclerView.Adapter<RecommendedLoanAdapter.Viewholder> {

    ArrayList<Loans> items;
    Context context;

    public RecommendedLoanAdapter(ArrayList<Loans> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_loan,parent,false);
        context = parent.getContext();
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, @SuppressLint("RecyclerView") int position) {
        if(items.get(position).getStatus().equals("attivo"))
        {
            holder.textEmailUserLoan.setText(items.get(position).getUser().getUsername());
            holder.textDataLoan.setText(items.get(position).getFormattedStartDate().toString());
            holder.textDueData.setText(items.get(position).getFormattedDueDate().toString());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView    textEmailUserLoan,textDataLoan,textDueData;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            textEmailUserLoan = itemView.findViewById(R.id.textViewEmailUserLoan);
            textDataLoan = itemView.findViewById(R.id.textViewDataLoan);
            textDueData = itemView.findViewById(R.id.textViewDuaData);
        }
    }
}
