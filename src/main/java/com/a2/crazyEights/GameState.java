package com.a2.crazyEights;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 234L;

    protected ArrayList<Player> players = new ArrayList<>();
    protected Card topCard;
    protected boolean gameStarted = false;

    protected int whoseTurn = -1;
    protected int prevTurn = 0;

    protected boolean skipTurn = false;
    protected int howManyPlayed = 0;

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

    public boolean canPlayerPlayEnough(int pid){
        Player p = players.get(pid - 1);

        // For every card check all combinations
        for (Card c : p.cards){
            if (c.isSuitOrRank(topCard)){
                int innerCount = 1;

                for (Card cc : p.cards){
                    if (!cc.equals(c)){
                        if (c.isSuitOrRank(cc)){
                            innerCount++;
                        }
                    }
                }

                if (innerCount >= howManyPlayed){
                    return true;
                }
            }
        }

        return false;
    }

    public void setHowManyPlayed(int howMany){
        howManyPlayed += howMany;
    }

    public void clearHowManyPlayed(){
        howManyPlayed = 0;
    }

    public void startGame(){
        int index = 0;

        for (Card c : allCards){
            if (!c.rank.equals("8")){
                this.setTopCard(allCards.get(index));
                break;
            }
            index++;
        }

        allCards.remove(index);
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


    public void setSkipTurn(boolean tf){
        skipTurn = tf;
    }

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

    public void playerDrawCards(int pid){
        for (int i = 0; i < howManyPlayed; i++){
            playerDrawCard(pid);
        }
    }

    public void playerRemoveCard(int pid, int i){
        players.get(pid - 1).removeCard(i);
    }

    public void playerRemoveCard(int pid, Card c){
        int count = 0;

        for (Card ca : players.get(pid - 1).cards){
            if (ca.equals(c)){
                playerRemoveCard(pid, count);
                System.out.println("CARD REMOVED!!");
            }
            count++;
        }
    }

    public boolean isRoundRunning(){
        boolean isRunning = true;

        for (Player p : players){
            if (p.cards.size() < 1){
                isRunning = false;
            }
        }

        if (allCards.size() < 1) isRunning = false;

        return isRunning;
    }

    public void tallyScores(){
        for (Player p : players) {
            p.clearScore();

            for (Card c : p.cards) {
                switch (c.rank) {
                    case "8":
                        p.addToScore(50);
                        break;
                    case "K":
                    case "J":
                    case "Q":
                        p.addToScore(10);
                        break;
                    case "A":
                        p.addToScore(1);
                        break;
                    default:
                        p.addToScore(Integer.parseInt(c.rank));
                        break;
                }
            }
        }
    }

    public boolean getCanAnyonePlay(){
        int howManyCan = 0;

        for (Player p : players){
            if (canPlayCard(p.pid)){
                howManyCan++;
            }
        }

        return howManyCan >= 1;
    }

    public String getScoreBoard(){
        StringBuilder rs = new StringBuilder();
        String direction = "Left";

        String topCardString = "";

        topCardString += topCard.rank;
        topCardString += topCard.suit;

        if (!directionForward){
            direction = "Right";
        }

        rs.append("\n    --Game Update--  \n");
        rs.append("_______________________\n");
        rs.append("| ROUND: " + roundNumber + "           |\n");
        rs.append("| Cards in pile: " + allCards.size() + "\n");
        rs.append("| Whose Turn: Player " + whoseTurn + " \n");
        rs.append("| Top Card:    " + topCardString + "\n");
        rs.append("| Direction:   " + direction + "\n");
        rs.append("_______________________\n");
        rs.append("| SCORES:             |\n");

        tallyScores();

        for (Player p : players){
            rs.append("| Player ").append(p.pid).append(" ~ ").append(p.score).append("\n");
        }

        rs.append("|                     |\n");
        rs.append("-----------------------\n");

        return rs.toString();
    }
}
