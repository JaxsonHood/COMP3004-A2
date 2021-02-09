package com.a2.crazyEights;

public class Card {
    String rank;
    String suit;

    Card(String rank, String suit){
        this.rank = rank;
        this.suit = suit;
    }

    public void print(){
        System.out.println(" ___ \n" +
                "|" + this.rank + "  |\n" +
                "|  " + this.suit + "|\n" +
                " --- \n");
    }

    public String[] getCardStringRows(){
        String[] cardRows = new String[4];

        cardRows[0] = " ___ ";

        if (this.rank.length() <= 1) {
            cardRows[1] = "|" + this.rank + "  |";
        } else {cardRows[1] = "|" + this.rank + " |";}

        cardRows[2] = "|  " + this.suit + "|";
        cardRows[3] = " --- ";

        return cardRows;
    }
}
