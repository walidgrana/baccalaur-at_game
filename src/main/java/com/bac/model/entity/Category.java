package com.bac.model.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une catégorie de jeu
 */
@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    private String id;
    
    @Column(name = "name", unique = true, nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_active")
    private boolean active;
    
    @Column(name = "display_order")
    private int displayOrder;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Word> words = new ArrayList<>();
    
    public Category() {
        this.id = UUID.randomUUID().toString();
        this.active = true;
        this.displayOrder = 0;
    }
    
    public Category(String name) {
        this();
        this.name = name;
    }
    
    public Category(String name, String description) {
        this(name);
        this.description = description;
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public int getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public List<Word> getWords() {
        return words;
    }
    
    public void setWords(List<Word> words) {
        this.words = words;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id != null && id.equals(category.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
