package com.bac.model.dao;

import com.bac.model.entity.Category;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité Category
 */
public class CategoryDAO extends BaseDAO<Category> {
    
    public CategoryDAO() {
        super(Category.class);
    }
    
    public Optional<Category> findByName(String name) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Category c WHERE c.name = :name", Category.class)
                    .setParameter("name", name)
                    .uniqueResultOptional();
        }
    }
    
    public List<Category> findActiveCategories() {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Category c WHERE c.active = true ORDER BY c.displayOrder", Category.class)
                    .list();
        }
    }
    
    public Category findOrCreate(String name) {
        return findByName(name).orElseGet(() -> {
            Category newCategory = new Category(name);
            return save(newCategory);
        });
    }
    
    public void updateDisplayOrder(String categoryId, int newOrder) {
        findById(categoryId).ifPresent(category -> {
            category.setDisplayOrder(newOrder);
            update(category);
        });
    }
    
    public void toggleActive(String categoryId) {
        findById(categoryId).ifPresent(category -> {
            category.setActive(!category.isActive());
            update(category);
        });
    }
    
    public void initDefaultCategories() {
        String[] defaults = {"Prénom", "Animal", "Pays", "Ville", "Fruit", "Métier", "Objet", "Plante"};
        int order = 0;
        for (String name : defaults) {
            try {
                if (findByName(name).isEmpty()) {
                    Category cat = new Category(name);
                    cat.setDisplayOrder(order++);
                    save(cat);
                }
            } catch (Exception e) {
                System.err.println("Catégorie '" + name + "' existe déjà ou erreur: " + e.getMessage());
            }
        }
    }
}
