package com.planit.model;

public class Task {
    private String title;
    private String description;
    private String cardSet; // Seçilen kart setinin adını tutacak (örn: "FIBONACCI")

    // Boş constructor, JSON'dan objeye çevirme (deserialization) işlemleri için gereklidir.
    public Task() {
    }
    
    // Tüm alanları içeren constructor
    public Task(String title, String description, String cardSet) {
        this.title = title;
        this.description = description;
        this.cardSet = cardSet;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCardSet() {
        return cardSet;
    }

    public void setCardSet(String cardSet) {
        this.cardSet = cardSet;
    }
}