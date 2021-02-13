package com.a2.crazyEights;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
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

                    if (o instanceof GameState){
                        GameState gs = (GameState) o;

                        if (!gameStarted){
                            System.out.println("\n___ GAME HAS STARTED ___");
                            gameStarted = true;
                        }

                        if (gs.whoseTurn != playerPid){
                            System.out.println("\n//// Player " + gs.whoseTurn + " is up ////");
                        } else {
                            System.out.println("\n////////// You are up //////////\n");

                            System.out.println("TOP CARD:");
                            gs.topCard.print();

                            if (!gs.canPlayCard(playerPid)){
                                System.out.println("You do not have a card to play, drawing card...");
                            }

                            int cardsDrawn = 0;

                            // Draw a card until player can play a card
                            while (!gs.canPlayCard(playerPid) && cardsDrawn < 2){
                                gs.playerDrawCard(playerPid);
                                cardsDrawn++;
                                System.out.println( cardsDrawn + " card(s) drawn");
                            }

                            // Get new updated player cards
                            player = gs.getPlayer(playerPid);
                            player.printCards();

                            boolean isCardSelected = false;

                            // Get player to select a card
                            while (!isCardSelected) {
                                System.out.print("\nSelect card (1 - " + player.cards.size() + ") : ");

                                String whichCardString = scanner.nextLine();

                                try {
                                    int whichCard = Integer.parseInt(whichCardString);

                                    if (whichCard > 0 && whichCard <= player.cards.size()){

                                        System.out.println("\nYou selected: ");

                                        Card c = player.getCard(whichCard - 1);
                                        c.print();

                                        System.out.println("\n --- Turn Ended, Player " + gs.getNextTurn() + " is up! --- ");
                                        isCardSelected = true;

                                        gs.setTopCard(c);
                                        gs.playerRemoveCard(playerPid, whichCard - 1);
                                        gs.getPlayer(playerPid).printCards();
                                        gs.setNextTurn();

                                        // So the ArrayLists get updated
                                        oos.reset();

                                        oos.writeObject(gs);

                                    } else System.out.println("Selected card out of number range...");

                                } catch (Exception e){
                                    System.out.println("Invalid Input...");
                                }
                            }

                        }
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
