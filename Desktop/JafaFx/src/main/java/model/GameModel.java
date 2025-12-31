package model;

import java.util.ArrayList;
import java.util.List;


public class GameModel {
    private List<Category> categories = new ArrayList<>();
    private char currentLetter;
    public GameModel() {
        categories.add(new Category("Pays"));
        categories.add(new Category("Ville"));
        categories.add(new Category("Animal"));
    }
    public List<Category> getCategories() { return categories; }
    public char getCurrentLetter() { return currentLetter; }
    public void setCurrentLetter(char c) { this.currentLetter = c; }
}
