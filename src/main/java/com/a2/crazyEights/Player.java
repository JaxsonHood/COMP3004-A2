package com.a2.crazyEights;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    private static final long serialVersionUID = 234L;

    int pid = -1;
    boolean isReady = false;

    protected ArrayList<Card> cards = new ArrayList<>();
    protected int score = 0;
    protected int totalScore = 0;

    public void addCard(Card card){
        cards.add(card);
    }

    public void removeCard(int i){
        cards.remove(i);
    }

    public void setCardList(ArrayList<Card> newCards){
        cards = newCards;
    }

    public Card getCard(int i){
        return cards.get(i);
    }

    public void printCards(){
        // Print out this players cards in a pretty way
        System.out.println("\nYOUR CARDS:");

        for (int i = 0; i < 4; i++){
            for (Card card : cards){
                System.out.print(card.getCardStringRows()[i] + "   ");
            }
            System.out.print("\n");
        }
    }

    public void setReady(boolean tf){
        isReady = tf;
    }

    public boolean getReady(){
        return isReady;
    }

    public void addToScore(int scor){
        score += scor;
    }

    public void clearScore(){
        score = 0;
    }

    public void addToTotalScore(){
        totalScore += score;
    }

    public void clearCards(){
        cards.clear();
    }
}
