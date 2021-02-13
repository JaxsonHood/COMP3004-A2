package com.a2.crazyEights;

import java.io.Serializable;

public class Card implements Serializable {
    private static final long serialVersionUID = 234L;

    String rank;
    String suit;

    private boolean isInPlay = true;

    Card(String rank, String suit){
        this.rank = rank;
        this.suit = suit;
    }

    public void print(){

        if (this.rank.length() <= 1) {
            System.out.println(" ___ \n" +
                    "|" + this.rank + "  |\n" +
                    "|  " + getSuit() + "|\n" +
                    " --- ");
        } else {  System.out.println(" ___ \n" +
                "|" + this.rank + " |\n" +
                "|  " + getSuit() + "|\n" +
                " --- ");}
    }

    public String getSuit(){
        String returnSuit;

        switch (this.suit) {
            case "H":
                returnSuit = "\u2665";
                break;
            case "S":
                returnSuit = "\u2660";
                break;
            case "D":
                returnSuit = "\u2666";
                break;
            default:
                returnSuit = "\u2663";
                break;
        }

        return returnSuit;
    }

    public boolean isSuitOrRank(Card c){
        if (c.rank.equals(rank) || c.suit.equals(suit)){
            return true;
        }
        return false;
    }

    public String[] getCardStringRows(){
        String[] cardRows = new String[4];

        cardRows[0] = " ___ ";

        if (this.rank.length() <= 1) {
            cardRows[1] = "|" + this.rank + "  |";
        } else {cardRows[1] = "|" + this.rank + " |";}

        cardRows[2] = "|  " + getSuit() + "|";
        cardRows[3] = " --- ";

        return cardRows;
    }

    public void setInPlay(boolean tf){
        isInPlay = tf;
    }

    public boolean getInPlay(){
        return isInPlay;
    }
}
