package com.bac.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour la validation des mots via APIs externes
 * Utilise des APIs spécifiques par catégorie + Gemini pour les autres
 */
public class ApiService {
    
    private static ApiService instance;
    private final HttpClient httpClient;
    
    // Clé API Gemini
    private static final String GEMINI_API_KEY = "AIzaSyBTPcqZE0CvmPgdTQv-22uUQyWsmhrSnGs";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";
    
    // APIs spécifiques par catégorie
    private static final String REST_COUNTRIES_API = "https://restcountries.com/v3.1/name/";
    private static final String NOMINATIM_API = "https://nominatim.openstreetmap.org/search?format=json&city=";
    private static final String GENDERIZE_API = "https://api.genderize.io?name=";
    private static final String WIKIPEDIA_FR_API = "https://fr.wikipedia.org/w/api.php?action=query&format=json&titles=";
    
    private ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }
    
    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }
    
    /**
     * Valide un mot selon sa catégorie en utilisant l'API appropriée
     */
    public boolean validateWord(String word, String category) {
        if (word == null || word.trim().isEmpty()) {
            return false;
        }
        
        String cleanWord = word.trim();
        String categoryLower = category.toLowerCase();
        
        try {
            // Sélectionner l'API selon la catégorie
            switch (categoryLower) {
                case "pays":
                    return validateCountry(cleanWord);
                case "ville":
                    return validateCity(cleanWord);
                case "prénom":
                case "prenom":
                    return validateFirstName(cleanWord);
                case "animal":
                    return validateWithWikipedia(cleanWord, "animal");
                case "fruit":
                    return validateWithWikipedia(cleanWord, "fruit");
                case "plante":
                    return validateWithWikipedia(cleanWord, "plante");
                default:
                    // Pour toutes les autres catégories, utiliser Gemini
                    return validateWithGemini(cleanWord, category);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la validation de '" + word + "' pour " + category + ": " + e.getMessage());
            // En cas d'erreur, utiliser Gemini comme fallback
            try {
                return validateWithGemini(cleanWord, category);
            } catch (Exception ex) {
                return false;
            }
        }
    }
    
    /**
     * Valide un pays via REST Countries API
     */
    private boolean validateCountry(String country) {
        try {
            String encodedCountry = URLEncoder.encode(country, StandardCharsets.UTF_8);
            String url = REST_COUNTRIES_API + encodedCountry;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonArray countries = JsonParser.parseString(body).getAsJsonArray();
                
                // Vérifier si le pays existe dans les résultats
                for (JsonElement elem : countries) {
                    JsonObject countryObj = elem.getAsJsonObject();
                    
                    // Vérifier le nom commun
                    if (countryObj.has("name")) {
                        JsonObject nameObj = countryObj.getAsJsonObject("name");
                        if (nameObj.has("common")) {
                            String commonName = nameObj.get("common").getAsString().toLowerCase();
                            if (commonName.contains(country.toLowerCase()) || 
                                country.toLowerCase().contains(commonName)) {
                                return true;
                            }
                        }
                        // Vérifier les traductions françaises
                        if (nameObj.has("nativeName")) {
                            JsonObject nativeNames = nameObj.getAsJsonObject("nativeName");
                            if (nativeNames.has("fra")) {
                                String frenchName = nativeNames.getAsJsonObject("fra")
                                        .get("common").getAsString().toLowerCase();
                                if (frenchName.contains(country.toLowerCase()) ||
                                    country.toLowerCase().contains(frenchName)) {
                                    return true;
                                }
                            }
                        }
                    }
                    
                    // Vérifier les traductions
                    if (countryObj.has("translations") && 
                        countryObj.getAsJsonObject("translations").has("fra")) {
                        String frenchName = countryObj.getAsJsonObject("translations")
                                .getAsJsonObject("fra").get("common").getAsString().toLowerCase();
                        if (frenchName.equalsIgnoreCase(country) ||
                            frenchName.contains(country.toLowerCase())) {
                            return true;
                        }
                    }
                }
                return true; // Au moins un pays trouvé
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur REST Countries API: " + e.getMessage());
            return validateWithGemini(country, "pays");
        }
    }
    
    /**
     * Valide une ville via Nominatim OpenStreetMap API
     */
    private boolean validateCity(String city) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String url = NOMINATIM_API + encodedCity;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .header("User-Agent", "BaccalaureatGame/1.0")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonArray results = JsonParser.parseString(body).getAsJsonArray();
                return results.size() > 0;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur Nominatim API: " + e.getMessage());
            return validateWithGemini(city, "ville");
        }
    }
    
    /**
     * Valide un prénom via Genderize API
     */
    private boolean validateFirstName(String firstName) {
        try {
            String encodedName = URLEncoder.encode(firstName, StandardCharsets.UTF_8);
            String url = GENDERIZE_API + encodedName;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonObject result = JsonParser.parseString(body).getAsJsonObject();
                
                // Si l'API retourne un genre (non null), le prénom est reconnu
                if (result.has("gender") && !result.get("gender").isJsonNull()) {
                    // Vérifier aussi la probabilité
                    if (result.has("probability")) {
                        double probability = result.get("probability").getAsDouble();
                        return probability > 0.3; // Seuil de confiance minimal
                    }
                    return true;
                }
                // Si pas de genre trouvé, utiliser Gemini
                return validateWithGemini(firstName, "prénom");
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur Genderize API: " + e.getMessage());
            return validateWithGemini(firstName, "prénom");
        }
    }
    
    /**
     * Valide un mot via Wikipedia FR API (pour animaux, fruits, plantes)
     */
    private boolean validateWithWikipedia(String word, String expectedType) {
        try {
            String encodedWord = URLEncoder.encode(capitalizeFirst(word), StandardCharsets.UTF_8);
            String url = WIKIPEDIA_FR_API + encodedWord;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonObject result = JsonParser.parseString(body).getAsJsonObject();
                
                if (result.has("query") && result.getAsJsonObject("query").has("pages")) {
                    JsonObject pages = result.getAsJsonObject("query").getAsJsonObject("pages");
                    
                    // Si la page existe (pas de -1)
                    for (String key : pages.keySet()) {
                        if (!key.equals("-1")) {
                            // La page existe sur Wikipedia, mais on vérifie avec Gemini 
                            // si c'est bien le bon type (animal, fruit, etc.)
                            return validateWithGemini(word, expectedType);
                        }
                    }
                }
            }
            return validateWithGemini(word, expectedType);
        } catch (Exception e) {
            System.err.println("Erreur Wikipedia API: " + e.getMessage());
            return validateWithGemini(word, expectedType);
        }
    }
    
    /**
     * Valide un mot via l'API Gemini (pour toutes les catégories ou en fallback)
     */
    public boolean validateWithGemini(String word, String category) {
        try {
            String prompt = createValidationPrompt(word, category);
            
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);
            
            // Configuration de génération pour réponse courte
            JsonObject generationConfig = new JsonObject();
            generationConfig.addProperty("temperature", 0.1);
            generationConfig.addProperty("maxOutputTokens", 10);
            requestBody.add("generationConfig", generationConfig);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_API_URL + GEMINI_API_KEY))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                JsonObject jsonResponse = JsonParser.parseString(body).getAsJsonObject();
                
                if (jsonResponse.has("candidates")) {
                    JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
                    if (candidates.size() > 0) {
                        JsonObject candidate = candidates.get(0).getAsJsonObject();
                        if (candidate.has("content")) {
                            JsonObject contentResp = candidate.getAsJsonObject("content");
                            if (contentResp.has("parts")) {
                                JsonArray partsResp = contentResp.getAsJsonArray("parts");
                                if (partsResp.size() > 0) {
                                    String text = partsResp.get(0).getAsJsonObject()
                                            .get("text").getAsString().trim().toLowerCase();
                                    
                                    // Vérifier si la réponse est positive
                                    return text.contains("oui") || text.contains("yes") || 
                                           text.contains("vrai") || text.contains("true") ||
                                           text.equals("1");
                                }
                            }
                        }
                    }
                }
            } else {
                System.err.println("Erreur Gemini API: " + response.statusCode() + " - " + response.body());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Erreur Gemini API: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Crée le prompt de validation pour Gemini
     */
    private String createValidationPrompt(String word, String category) {
        String categoryDesc = getCategoryDescription(category);
        return String.format(
            "Réponds uniquement par 'OUI' ou 'NON'. Est-ce que '%s' est un(e) %s valide et existant(e) ? " +
            "Le mot doit être un vrai %s reconnu, pas un mot inventé.",
            word, categoryDesc, categoryDesc
        );
    }
    
    /**
     * Obtient la description de la catégorie pour le prompt
     */
    private String getCategoryDescription(String category) {
        switch (category.toLowerCase()) {
            case "pays": return "pays du monde";
            case "ville": return "ville existante";
            case "prénom":
            case "prenom": return "prénom de personne";
            case "animal": return "animal (espèce animale)";
            case "fruit": return "fruit comestible";
            case "légume":
            case "legume": return "légume";
            case "plante": return "plante (végétal)";
            case "métier":
            case "metier": return "métier ou profession";
            case "objet": return "objet physique";
            case "marque": return "marque commerciale connue";
            case "sport": return "sport ou activité sportive";
            case "couleur": return "couleur";
            case "instrument": return "instrument de musique";
            default: return category;
        }
    }
    
    /**
     * Capitalise la première lettre d'un mot
     */
    private String capitalizeFirst(String word) {
        if (word == null || word.isEmpty()) return word;
        return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
    }
    
    /**
     * Validation asynchrone d'un mot
     */
    public CompletableFuture<Boolean> validateWordAsync(String word, String category) {
        return CompletableFuture.supplyAsync(() -> validateWord(word, category));
    }
    
    /**
     * Vérifie si les APIs sont accessibles
     */
    public boolean isApiReachable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://restcountries.com/v3.1/name/france"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
