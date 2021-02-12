package com.a2.crazyEights;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class GameState implements Serializable {
    private static final long serialVersionUID = 234L;

    protected ArrayList<Player> players;
    protected Card topCard;
    protected boolean gameStarted = false;

    private final String[] suits = {"S", "H", "D", "C"};
    private final String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    // Store all the cards in the deck
    public ArrayList<Card> allCards = new ArrayList<>();

    GameState(){
        players = new ArrayList<>();
        this.populateDeck();
    }

    public boolean isReady(){
        int count = 0;

        for (Player p : players){
            if (p.getReady()){
                count++;
            }
        }

        return count == players.size();
    }

    public void addPlayer(Player p){
        players.add(p);
        System.out.println(players.size());
        this.assignCards(p.pid - 1);
    }

    public void setTopCard(Card topCard){
        this.topCard = topCard;
    }

    public void startGame(){
        gameStarted = true;
    }

    public boolean isRunning(){
        return gameStarted;
    }

    public void addCardToPlayer(Card c, int pos){
        players.get(pos).addCard(c);
    }

    public ArrayList<Player> getPlayers(){
        return players;
    }

    public void setPlayerReady(int i){
        players.get(i).setReady(true);
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

        // Shuffle the deck
        Collections.shuffle(allCards);
    }

    public void assignCards(int i){
        int count = 0;

        ArrayList<Card> newCardsAvailable = new ArrayList<>();

        for (Card c : allCards){
            if (count < 5){
                this.addCardToPlayer(c, i);
            } else {newCardsAvailable.add(c);}
            count++;
        }

        allCards = newCardsAvailable;
    }
}
