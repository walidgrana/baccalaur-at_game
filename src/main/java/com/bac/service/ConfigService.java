package com.bac.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Service de configuration de l'application
 */
public class ConfigService {
    
    private static ConfigService instance;
    private Properties properties;
    
    private ConfigService() {
        properties = new Properties();
        loadProperties();
    }
    
    public static synchronized ConfigService getInstance() {
        if (instance == null) {
            instance = new ConfigService();
        }
        return instance;
    }
    
    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Fichier config.properties non trouvé, utilisation des valeurs par défaut");
                setDefaultProperties();
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Erreur de chargement de la configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        properties.setProperty("api.dictionary.url", "https://api.dictionaryapi.dev/api/v2/entries/en/");
        properties.setProperty("api.dictionary.enabled", "true");
        properties.setProperty("server.port", "5555");
        properties.setProperty("server.timeout", "30000");
        properties.setProperty("game.timer.seconds", "120");
        properties.setProperty("game.default.categories", "Prénom,Animal,Pays,Ville,Fruit,Métier,Objet,Plante");
    }
    
    public String getApiUrl() {
        return properties.getProperty("api.dictionary.url", "https://api.dictionaryapi.dev/api/v2/entries/en/");
    }
    
    public void setApiUrl(String url) {
        properties.setProperty("api.dictionary.url", url);
    }
    
    public boolean isApiEnabled() {
        return Boolean.parseBoolean(properties.getProperty("api.dictionary.enabled", "true"));
    }
    
    public void setApiEnabled(boolean enabled) {
        properties.setProperty("api.dictionary.enabled", String.valueOf(enabled));
    }
    
    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "5555"));
    }
    
    public void setServerPort(int port) {
        properties.setProperty("server.port", String.valueOf(port));
    }
    
    public int getServerTimeout() {
        return Integer.parseInt(properties.getProperty("server.timeout", "30000"));
    }
    
    public int getGameTimerSeconds() {
        return Integer.parseInt(properties.getProperty("game.timer.seconds", "120"));
    }
    
    public void setGameTimerSeconds(int seconds) {
        properties.setProperty("game.timer.seconds", String.valueOf(seconds));
    }
    
    public String[] getDefaultCategories() {
        String cats = properties.getProperty("game.default.categories", "Prénom,Animal,Pays,Ville,Fruit,Métier,Objet,Plante");
        return cats.split(",");
    }
    
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
