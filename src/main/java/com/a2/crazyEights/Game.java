package com.a2.crazyEights;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Game implements Serializable {
    private static final long serialVersionUID = 2L;

    private int playerPid = -1;
    private Player player;
    private GameState gameState;

    public static void main(String[] args) throws ClassNotFoundException{
        Game g = new Game();
        g.connectSocket();
    }

    Game(){
        System.out.println("--- | Welcome to CrazyEights | ---");
    }

    public void connectSocket() throws ClassNotFoundException{
        String hostname = "localhost";
        int port = 9200;

        try (Socket socket = new Socket(hostname, port)) {

            Scanner scanner = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Please enter your name: ");
            String name = scanner.nextLine();  // Read user input

            // get the output stream from the socket.
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            ObjectInputStream ois = new ObjectInputStream(inputStream);

            player = new Player(name);
            oos.writeObject(player);

            Object o;
            String scan = "";

            while (socket.isConnected()) {
                o = ois.readObject();

                if (o instanceof Player){
                    Player p = (Player) o;

                    if (this.playerPid == -1 && !p.name.equals("null")){

                        this.playerPid = p.pid;
                        player = p;

                        System.out.println("Connected Successfully, you are Player " + this.playerPid);
                        System.out.println("Type 'ready' to ready-up for the game :)");

                    } else if (this.playerPid != -1 && !p.name.equals(name)) {
                        System.out.println(p.name + " joined as Player " + p.pid);

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

                    if (!gs.equals(gameState)){
                        System.out.println("GAME STATE UPDATED!!!");
                        gameState = gs;
                    }
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
}
