package com.bac.model.dao;

import com.bac.model.entity.Player;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité Player
 */
public class PlayerDAO extends BaseDAO<Player> {
    
    public PlayerDAO() {
        super(Player.class);
    }
    
    public Optional<Player> findByPseudo(String pseudo) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Player p WHERE p.pseudo = :pseudo", Player.class)
                    .setParameter("pseudo", pseudo)
                    .uniqueResultOptional();
        }
    }
    
    public Player findOrCreate(String pseudo) {
        return findByPseudo(pseudo).orElseGet(() -> {
            Player newPlayer = new Player(pseudo);
            return save(newPlayer);
        });
    }
    
    public List<Player> findTopPlayers(int limit) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Player p ORDER BY p.totalScore DESC", Player.class)
                    .setMaxResults(limit)
                    .list();
        }
    }
    
    public List<Player> findByGamesWon() {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM Player p ORDER BY p.gamesWon DESC", Player.class)
                    .list();
        }
    }
}
