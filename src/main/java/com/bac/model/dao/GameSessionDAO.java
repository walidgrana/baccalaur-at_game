package com.bac.model.dao;

import com.bac.model.entity.GameSession;
import com.bac.model.entity.GameSession.GameMode;
import com.bac.model.entity.GameSession.GameStatus;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

/**
 * DAO pour l'entité GameSession
 */
public class GameSessionDAO extends BaseDAO<GameSession> {
    
    public GameSessionDAO() {
        super(GameSession.class);
    }
    
    public Optional<GameSession> findBySessionCode(String sessionCode) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM GameSession gs WHERE gs.sessionCode = :code", GameSession.class)
                    .setParameter("code", sessionCode)
                    .uniqueResultOptional();
        }
    }
    
    public List<GameSession> findByStatus(GameStatus status) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM GameSession gs WHERE gs.status = :status ORDER BY gs.startedAt DESC", GameSession.class)
                    .setParameter("status", status)
                    .list();
        }
    }
    
    public List<GameSession> findByGameMode(GameMode mode) {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM GameSession gs WHERE gs.gameMode = :mode ORDER BY gs.startedAt DESC", GameSession.class)
                    .setParameter("mode", mode)
                    .list();
        }
    }
    
    public List<GameSession> findWaitingMultiplayerSessions() {
        try (Session session = getSession()) {
            return session.createQuery(
                    "FROM GameSession gs WHERE gs.gameMode = :mode AND gs.status = :status", GameSession.class)
                    .setParameter("mode", GameMode.MULTIPLAYER)
                    .setParameter("status", GameStatus.WAITING)
                    .list();
        }
    }
    
    public String generateUniqueSessionCode() {
        String code;
        do {
            code = generateRandomCode();
        } while (findBySessionCode(code).isPresent());
        return code;
    }
    
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
