package com.bac.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant un mot validé
 */
@Entity
@Table(name = "words", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"word", "category_id"})
})
public class Word {
    
    @Id
    private String id;
    
    @Column(name = "word", nullable = false)
    private String word;
    
    @Column(name = "first_letter")
    private Character firstLetter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "is_valid")
    private boolean valid;
    
    @Column(name = "validated_at")
    private LocalDateTime validatedAt;
    
    @Column(name = "validation_source")
    private String validationSource; // "LOCAL" ou "API"
    
    public Word() {
        this.id = UUID.randomUUID().toString();
        this.validatedAt = LocalDateTime.now();
        this.valid = true;
    }
    
    public Word(String word, Category category) {
        this();
        this.word = word.toLowerCase().trim();
        this.category = category;
        if (!word.isEmpty()) {
            this.firstLetter = Character.toUpperCase(word.charAt(0));
        }
    }
    
    // Getters et Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getWord() {
        return word;
    }
    
    public void setWord(String word) {
        this.word = word.toLowerCase().trim();
        if (!word.isEmpty()) {
            this.firstLetter = Character.toUpperCase(word.charAt(0));
        }
    }
    
    public Character getFirstLetter() {
        return firstLetter;
    }
    
    public void setFirstLetter(Character firstLetter) {
        this.firstLetter = firstLetter;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }
    
    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }
    
    public String getValidationSource() {
        return validationSource;
    }
    
    public void setValidationSource(String validationSource) {
        this.validationSource = validationSource;
    }
    
    @Override
    public String toString() {
        return word;
    }
}
