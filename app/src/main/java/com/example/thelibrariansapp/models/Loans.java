package com.example.thelibrariansapp.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Loans {

    private User user;
    private List<Book> books;
    private Date startDate;
    private Date dueDate;
    private String status;



    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date duedate) {
        this.dueDate = duedate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    // Getter e Setter per User
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

//    public LendLease(List<Book> books, Date startDate, Date duedate) {
//        this.books = books;
//        this.startDate = startDate;
//        this.dueDate = dueDate;
//    }
    public String getFormattedStartDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(startDate);
}

    public String getFormattedDueDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(dueDate);
    }
    public Loans(User user, List<Book> books, Date startDate, Date dueDate, String status) {
        this.user = user;
        this.books = books;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Loans() {

    }
}
