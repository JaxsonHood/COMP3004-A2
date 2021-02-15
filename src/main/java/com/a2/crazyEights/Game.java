package com.a2.crazyEights;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Game implements Serializable {
    private static final long serialVersionUID = 2L;

    private int playerPid = -1;
    private Player player;
    private boolean gameStarted = false;


    ObjectOutputStream oos;
    ObjectInputStream ois;

    public static void main(String[] args) {
        Game g = new Game();
        g.connectSocket();
    }

    Game(){
        System.out.println("--- | Welcome to CrazyEights | ---");
    }

    public void connectSocket() {
        String hostname = "localhost";
        int port = 9200;

        try (Socket socket = new Socket(hostname, port)) {

            Scanner scanner = new Scanner(System.in);  // Create a Scanner object

            // get the output stream from the socket.
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            oos = new ObjectOutputStream(outputStream);
            ois = new ObjectInputStream(inputStream);

            player = new Player();
            oos.writeObject(player);

            Object o;
            String scan = "";

            while (socket.isConnected()) {
                try {
                    o = ois.readObject();

                    if (o instanceof Player){
                        Player p = (Player) o;

                        if (this.playerPid == -1){

                            this.playerPid = p.pid;
                            player = p;

                            System.out.println("Connected Successfully, you are Player " + this.playerPid);
                            System.out.println("Type 'ready' to ready-up for the game :)");

                        } else { System.out.println("Sorry, game is full! You cannot join."); }
                    }

                    if (o instanceof Message){
                        Message m = (Message) o;
                        System.out.println(m.message);
                    }

                    if (!player.getReady()){
                        while (!scan.equals("ready")){
                            scan = scanner.nextLine();

                            if (scan.equals("ready")){
                                player.setReady(true);
                                oos.writeObject(player);
                            } else {
                                System.out.println("Check your input -- " + scan);
                            }
                        }
                    }

                    // GO in here for game
                    if (o instanceof GameState){
                        GameState gs = (GameState) o;

                        if (!gameStarted){
                            System.out.println("\n_____ GAME HAS STARTED _____\n");
                            gameStarted = true;
                        }

                        if (gs.whoseTurn != playerPid){
                            System.out.print(" ");
                        } else {
                            System.out.println(" --- Turn Started --- ");

                            // To check if previous player played an 8
                            if (!gs.getSuitToMatch().equals("")){
                                System.out.println("Previous player changed suit to : " + gs.getSuitToMatch());
                                System.out.println("Either play another 8 or match suit...");
                            } else {
                                System.out.println("TOP CARD:");
                                gs.topCard.print();
                            }

                            if (!gs.canPlayCard(playerPid)){
                                System.out.println("You do not have a card to play, drawing card...");
                            }

                            // Used for handling twos
                            boolean canPlayCardsLocal = false;
                            ArrayList<Card> cardsToBePlayed = new ArrayList<>();
                            ArrayList<Integer> choices = new ArrayList<>();

                            // Handles the twos
                            if (gs.howManyPlayed > 0 && gs.canPlayerPlayEnough(playerPid)){
                                System.out.println("\nPrevious player played a two, pick " + gs.howManyPlayed + " cards to play.");
                                canPlayCardsLocal = true;
                            } else if (gs.howManyPlayed > 0){
                                System.out.println("\nPrevious player played a two, you cannot play " + gs.howManyPlayed + " cards, draw " + gs.howManyPlayed + " cards.");
                                gs.playerDrawCards(playerPid);
                            }

                            int cardsDrawn = 0;

                            // Draw a card until player can play a card
                            while (!gs.canPlayCard(playerPid) && cardsDrawn < 3){
                                gs.playerDrawCard(playerPid);
                                cardsDrawn++;
                                System.out.println( cardsDrawn + " card(s) drawn");
                            }

                            // Get new updated player cards
                            player = gs.getPlayer(playerPid);
                            playerPid = player.pid;
                            player.printCards();

                            boolean isCardSelected = false;

                            String chooseToDraw = "";

                            if (cardsDrawn < 1 && gs.howManyPlayed < 1){
                                //Choose to draw
                                System.out.println("Do you want a draw a card? (y/n) : ");
                                chooseToDraw = scanner.nextLine();
                            }

                            if (chooseToDraw.equals("y") && cardsDrawn == 0){
                                gs.playerDrawCard(playerPid);
                                player = gs.getPlayer(playerPid);
                                player.printCards();
                            }

                            // Get player to select a card
                            if (gs.canPlayCard(playerPid) && !gs.skipTurn){
                                while (!isCardSelected || (canPlayCardsLocal && cardsToBePlayed.size() < gs.howManyPlayed)) {
                                    System.out.print("\nSelect card (1 - " + player.cards.size() + ") : ");

                                    String whichCardString = scanner.nextLine();

                                    try {
                                        int whichCard = Integer.parseInt(whichCardString);

                                        if (whichCard > 0 && whichCard <= player.cards.size()){

                                            Card c = player.getCard(whichCard - 1);

                                            if (c.isSuitOrRank(gs.topCard) || c.suit.equals(gs.getSuitToMatch()) || (cardsToBePlayed.size() > 0) || c.rank.equals("8")){

                                                System.out.println("\nYou selected: ");
                                                c.print();

                                                // Reset suitToMatch for next player
                                                gs.setSuitToMatch("");

                                                if (c.rank.equals("8")){
                                                    System.out.print("\nYou played an 8 select suit you want to change to {S, H, D, C} : ");
                                                    String newSuit = scanner.nextLine();
                                                    gs.setSuitToMatch(newSuit);
                                                    c.suit = newSuit;
                                                }

                                                if (c.rank.equals("A")){
                                                    gs.changeDirection();
                                                    System.out.println("You played an Ace: Direction Changed!!");
                                                }

                                                if (c.rank.equals("Q")){
                                                    gs.setSkipTurn(true);
                                                    System.out.println("You played a Queen skipping next player");
                                                }

                                                if (c.rank.equals("2")){
                                                    gs.setHowManyPlayed(2);
                                                }

                                                if (!canPlayCardsLocal){
                                                    System.out.println("\n --- Turn Ended, Player " + gs.getNextTurn() + " is up! --- ");
                                                    isCardSelected = true;

                                                    if (gs.topCard.rank.equals("2") && !c.rank.equals("2")){
                                                        gs.clearHowManyPlayed();
                                                    }

                                                    gs.setTopCard(c);
                                                    gs.playerRemoveCard(playerPid, whichCard - 1);
                                                    gs.setNextTurn();

                                                    // So the ArrayLists get updated
                                                    oos.reset();
                                                    oos.writeObject(gs);
                                                } else {
                                                    cardsToBePlayed.add(c);
                                                    choices.add(whichCard - 1);
                                                    if (cardsToBePlayed.size() == gs.howManyPlayed){
                                                        canPlayCardsLocal = false;
                                                        isCardSelected = true;
                                                    }
                                                }

                                                if (cardsToBePlayed.size() != 0 && cardsToBePlayed.size() == gs.howManyPlayed){

                                                    System.out.println("# of cards played: " + cardsToBePlayed.size());
                                                    System.out.println("\n --- Turn Ended, Player " + gs.getNextTurn() + " is up! --- ");
                                                    isCardSelected = true;

                                                    if (gs.topCard.rank.equals("2") && !c.rank.equals("2")){
                                                        gs.clearHowManyPlayed();
                                                    }

                                                    gs.setTopCard(c);

                                                    // TODO -- Check if there is a two in the cards chosen
                                                    for (int ch : choices){
                                                        if (player.getCard(ch).rank.equals("2")){
                                                            gs.setTopCard(player.getCard(ch));
                                                            gs.setHowManyPlayed(2);
                                                        }
                                                    }

                                                    //remove choices
                                                    gs.removeChoices(playerPid, cardsToBePlayed);
                                                    gs.setNextTurn();

                                                    // So the ArrayLists get updated
                                                    oos.reset();
                                                    oos.writeObject(gs);
                                                }

                                            } else System.out.println("Card is not playable, please select a different card...");

                                        } else System.out.println("Selected card out of number range...");

                                    } catch (Exception e){
                                        System.out.println("Invalid Input...");
                                        System.out.println(e.getMessage());
                                    }
                                }
                            } else {
                                if (gs.skipTurn){
                                    gs.setSkipTurn(false);
                                    System.out.println("\n - Previous player played a Queen, skip your turn -");
                                } else {
                                    System.out.println("\n - Cannot play any cards -");
                                }
                                System.out.println("\n --- Turn Ended, Player " + gs.getNextTurn() + " is up! --- ");

                                gs.setNextTurn();

                                // So the ArrayLists get updated
                                oos.reset();
                                oos.writeObject(gs);
                            }
                        }
                    }

                    // Show player updated score sheet
                    if (o instanceof String){
                        String scoreSheet = (String) o;
                        System.out.println(scoreSheet);
                    }

                } catch (ClassNotFoundException ex){
                    System.out.println(ex.getMessage());
                }
            }

            oos.close();
            ois.close();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public void readyUp(){
        try {
            player.setReady(true);
            oos.writeObject(player);
        } catch (IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}
