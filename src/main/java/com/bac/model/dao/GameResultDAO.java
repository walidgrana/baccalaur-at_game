package com.bac.model.dao;

import com.bac.model.entity.GameResult;
import com.bac.model.entity.GameSession;
import com.bac.model.entity.Player;
import org.hibernate.Session;
import java.util.List;

/**
 * DAO pour l'entité GameResult
 */
public class GameResultDAO extends BaseDAO<GameResult> {
    
    public GameResultDAO() {
        super(GameResult.class);
    }
    
    public List<GameResult> findByPlayer(Player player) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT gr FROM GameResult gr " +
                    "LEFT JOIN FETCH gr.gameSession " +
                    "WHERE gr.player.id = :playerId ORDER BY gr.createdAt DESC", GameResult.class)
                    .setParameter("playerId", player.getId())
                    .list();
        }
    }
    
    public List<GameResult> findByGameSession(GameSession gameSession) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM GameResult gr WHERE gr.gameSession = :session ORDER BY gr.score DESC", GameResult.class)
                    .setParameter("session", gameSession)
                    .list();
        }
    }
    
    public List<GameResult> findRecentByPlayer(Player player, int limit) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "SELECT DISTINCT gr FROM GameResult gr " +
                    "LEFT JOIN FETCH gr.gameSession " +
                    "WHERE gr.player.id = :playerId ORDER BY gr.createdAt DESC", GameResult.class)
                    .setParameter("playerId", player.getId())
                    .setMaxResults(limit)
                    .list();
        }
    }
    
    public int getTotalScoreByPlayer(Player player) {
        try (Session session = getSession()) {
            Long total = session.createQuery(
                    "SELECT COALESCE(SUM(gr.score), 0) FROM GameResult gr WHERE gr.player = :player", Long.class)
                    .setParameter("player", player)
                    .uniqueResult();
            return total != null ? total.intValue() : 0;
        }
    }
    
    public int countWinsByPlayer(Player player) {
        try (Session session = getSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(gr) FROM GameResult gr WHERE gr.player = :player AND gr.winner = true", Long.class)
                    .setParameter("player", player)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }
}
