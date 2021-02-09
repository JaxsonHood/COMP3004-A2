package com.a2.crazyEights;

import java.io.Serializable;
import java.util.ArrayList;

public class Server implements Serializable {
    private static final long serialVersionUID = 2L;

    public static void main(String[] args) {
        new Server();
    }

    // Store all the cards in the deck
    private ArrayList<Card> allCards = new ArrayList<>(52);

    // Store all the players in the game
    private ArrayList<Player> allPlayers = new ArrayList<>(4);

    public String[] suits = {"S", "H", "D", "C"};
    public String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    Server(){
        this.populateDeck();
        System.out.println("Deck Size: " + this.allCards.size());
    }

    public void populateDeck(){
        // Clear deck in case cards left over
        allCards.clear();

        /* Creating all possible cards... */
        for (String s : this.suits) {
            for (String r : this.ranks) {
                allCards.add(new Card(r, s));
            }
        }
    }
}
