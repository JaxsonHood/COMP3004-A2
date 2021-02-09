package com.a2.crazyEights;

import java.io.Serializable;
import java.util.ArrayList;

public class Player implements Serializable {
    private static final long serialVersionUID = 234L;

    String name;
    int pid;

    ArrayList<Card> cards = new ArrayList<>();

    Player(String n){
        name = n;
    }

    public void addCard(Card card){
        this.cards.add(card);
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
}
