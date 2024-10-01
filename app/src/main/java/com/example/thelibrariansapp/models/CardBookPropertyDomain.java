package com.example.thelibrariansapp.models;

import java.io.Serializable;

public class CardBookPropertyDomain implements Serializable {
    public void setTotalCopies(int totalCopies) {
        TotalCopies = totalCopies;
    }
    public void setCopiesInUse(int copiesInUse) {
        CopiesInUse = copiesInUse;
    }

    private int TotalCopies,AvailableCopies,CopiesInUse;
    public int getTotalCopies() {
        return TotalCopies;
    }

    public int getAvailableCopies() {
        return AvailableCopies;
    }
    public int getCopiesInUse() {
        return CopiesInUse;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public CardBookPropertyDomain(String title, String author, String ISBN, String category, String imageUrl, int totalcopies, int availablecopies, int copiesinuse) {
        this.title = title;
        this.Author = author;
        this.ISBN = ISBN;
        this.Category = category;
        this.ImageUrl = imageUrl;
        this.TotalCopies = totalcopies;
        this.AvailableCopies = availablecopies;
        this.CopiesInUse = copiesinuse;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    private String title;
    private String Author;
    private String ISBN;
    private String Category;
    private String ImageUrl;
}
