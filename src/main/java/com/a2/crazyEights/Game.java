package com.a2.crazyEights;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 2L;

    public static void main(String[] args) {
        new Game();
    }

    Game(){
        this.createPlayer();
    }

    public void createPlayer(){
        Player p = new Player("Player-1");

        p.addCard(new Card("J","C"));
        p.addCard(new Card("10", "S"));
        p.addCard(new Card("3", "D"));
        p.addCard(new Card("Q","S"));
        p.addCard(new Card("9", "H"));
        p.addCard(new Card("A", "H"));
        p.addCard(new Card("8", "S"));

        p.printCards();
    }
}
