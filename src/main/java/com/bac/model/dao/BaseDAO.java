package com.bac.model.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import java.util.Optional;

/**
 * DAO générique de base
 */
public abstract class BaseDAO<T> {
    
    protected final Class<T> entityClass;
    
    protected BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    protected Session getSession() {
        return HibernateUtil.getSessionFactory().openSession();
    }
    
    public T save(T entity) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            return entity;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    public T update(T entity) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            T mergedEntity = session.merge(entity);
            transaction.commit();
            return mergedEntity;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    public void delete(T entity) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSession();
            transaction = session.beginTransaction();
            session.remove(session.merge(entity));
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Erreur lors du rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    public Optional<T> findById(Object id) {
        try (Session session = getSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        }
    }
    
    public List<T> findAll() {
        try (Session session = getSession()) {
            return session.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                         .list();
        }
    }
    
    public void deleteById(Object id) {
        findById(id).ifPresent(this::delete);
    }
}
