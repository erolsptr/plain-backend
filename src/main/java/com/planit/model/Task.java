package com.planit.model;

// Şimdilik basit bir görev modeli.
// İleride daha fazla alan ekleyebiliriz (oluşturan, oluşturulma tarihi vs.)
public class Task {
    private String title;
    private String description;

    // Constructor, Getter, Setter
    public Task(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}