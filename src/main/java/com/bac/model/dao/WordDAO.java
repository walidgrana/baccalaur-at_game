package com.bac.model.dao;

import com.bac.model.entity.Category;
import com.bac.model.entity.Word;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité Word
 */
public class WordDAO extends BaseDAO<Word> {
    
    public WordDAO() {
        super(Word.class);
    }
    
    public Optional<Word> findByWordAndCategory(String word, Category category) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Word w WHERE LOWER(w.word) = :word AND w.category = :category", Word.class)
                    .setParameter("word", word.toLowerCase().trim())
                    .setParameter("category", category)
                    .uniqueResultOptional();
        }
    }
    
    public Optional<Word> findByWordAndCategoryName(String word, String categoryName) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Word w WHERE LOWER(w.word) = :word AND w.category.name = :categoryName", Word.class)
                    .setParameter("word", word.toLowerCase().trim())
                    .setParameter("categoryName", categoryName)
                    .uniqueResultOptional();
        }
    }
    
    public List<Word> findByFirstLetter(Character letter) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Word w WHERE w.firstLetter = :letter AND w.valid = true", Word.class)
                    .setParameter("letter", Character.toUpperCase(letter))
                    .list();
        }
    }
    
    public List<Word> findByCategory(Category category) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Word w WHERE w.category = :category AND w.valid = true", Word.class)
                    .setParameter("category", category)
                    .list();
        }
    }
    
    public List<Word> findByFirstLetterAndCategory(Character letter, Category category) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Word w WHERE w.firstLetter = :letter AND w.category = :category AND w.valid = true", Word.class)
                    .setParameter("letter", Character.toUpperCase(letter))
                    .setParameter("category", category)
                    .list();
        }
    }
    
    public boolean isWordValid(String word, Category category) {
        return findByWordAndCategory(word, category)
                .map(Word::isValid)
                .orElse(false);
    }
    
    public Word saveValidatedWord(String word, Category category, String source) {
        Word newWord = new Word(word, category);
        newWord.setValid(true);
        newWord.setValidationSource(source);
        return save(newWord);
    }
    
    public long countValidWords() {
        try (Session session = getSession()) {
            return session.createQuery(
                    "SELECT COUNT(w) FROM Word w WHERE w.valid = true", Long.class)
                    .uniqueResult();
        }
    }
}
