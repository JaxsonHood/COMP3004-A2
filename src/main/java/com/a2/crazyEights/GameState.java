package com.a2.crazyEights;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class GameState implements Serializable {
    private static final long serialVersionUID = 234L;

    protected ArrayList<Player> players = new ArrayList<>();
    protected Card topCard;
    protected boolean gameStarted = false;

    protected int whoseTurn = -1;
    protected int prevTurn = 0;

    protected boolean directionForward = true;
    protected int roundNumber = 1;

    protected String suitToMatch = "";

    private final String[] suits = {"S", "H", "D", "C"};
    private final String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    // Store all the cards in the deck
    public ArrayList<Card> allCards = new ArrayList<>();

    GameState(){
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
        this.assignCards(p.pid - 1);
    }

    public Player getPlayer(int pid){
        return players.get(pid - 1);
    }

    public void setTopCard(Card topCard){
        this.topCard = topCard;
    }

    public void startGame(){
        this.setTopCard(allCards.get(0));
        allCards.remove(0);
        gameStarted = true;
    }

    public boolean isRunning(){ return gameStarted; }

    public String getSuitToMatch(){
        return suitToMatch;
    }

    public void setSuitToMatch(String s){
        suitToMatch = s;
    }

    public void setNextTurn(){
        prevTurn = whoseTurn;
        whoseTurn = getNextTurn(); }

    public int getNextTurn(){
        if (directionForward){
            if (whoseTurn < players.size() && whoseTurn != -1){
                return whoseTurn + 1;
            }
            return 1;
        } else {
            if (whoseTurn > 1){
                return whoseTurn - 1;
            }
            return players.size();
        }
    }

    public int getPrevTurn(){
        return prevTurn;
    }

    public void changeDirection(){
        directionForward = !directionForward;
    }

    public void addCardToPlayer(Card c, int pos){
        players.get(pos).addCard(c);
    }

    public boolean canPlayCard(int pid){
        Player p = players.get(pid - 1);

        if (!getSuitToMatch().equals("")){
            for (Card c : p.cards){
                if (c.suit.equals(getSuitToMatch())){
                    return true;
                }
            }
        } else {
            for (Card c : p.cards){
                if (c.isSuitOrRank(topCard)){
                    return true;
                }
            }
        }

        return false;
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

    public void playerDrawCard(int pid){
        Player p = players.get(pid - 1);
        p.addCard(allCards.get(0));
        players.set(pid - 1, p);

        allCards.remove(0);
    }

    public void playerRemoveCard(int pid, int i){
        players.get(pid - 1).removeCard(i);
    }
}
