package com.a2.crazyEights;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {

    protected int          serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;

    protected ArrayList<Player> players = new ArrayList<>();
    protected ArrayList<WorkerRunnable> runnables = new ArrayList<>();

    private final String[] suits = {"S", "H", "D", "C"};
    private final String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    // Store all the cards in the deck
    protected ArrayList<Card> allCards = new ArrayList<>();

    public static void main(String[] args) {
        Server server = new Server(9200);
        new Thread(server).start();
    }

    public Server(int port){
        this.serverPort = port;
        this.populateDeck();
    }

    public void run(){
        synchronized(this){
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while(players.size() < 4){
            Socket clientSocket;

            try {
                clientSocket = this.serverSocket.accept();
                System.out.println("Connection Accepted...");
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }

            WorkerRunnable wr = new WorkerRunnable(clientSocket, this);
            runnables.add(wr);
            new Thread(runnables.get(runnables.size() - 1)).start();
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop(){
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port 9200", e);
        }
    }

    public void addPlayer(Player p){
        players.add(p);
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
    }
}

class WorkerRunnable implements Runnable{

    protected Socket clientSocket;
    protected final Server server;

    protected Player player = null;
    protected ObjectOutputStream oos = null;

    public WorkerRunnable(Socket clientSocket, Server s) {
        this.clientSocket = clientSocket;
        this.server = s;
    }

    public void run() {
        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            ObjectInputStream ois = new ObjectInputStream(input);
            ObjectOutputStream oos = new ObjectOutputStream(output);

            this.oos = oos;

            ArrayList<Player> players;
            ArrayList<WorkerRunnable> runnables;

            while(clientSocket.isConnected()){
                Object o = ois.readObject();

                if (o instanceof Player){
                    //Update players
                    players = server.players;

                    Player newPlayer = (Player) o;
                    newPlayer.pid = players.size() + 1;

                    player = newPlayer;
                    server.addPlayer(newPlayer);

                    oos.writeObject(newPlayer);
                }

                runnables = server.runnables;

                if (player != null && runnables.size() > 0){
                    for (WorkerRunnable r : runnables){
                        if (r.player.pid != player.pid){
                            r.sendMessage(player);
                        }
                    }
                }
            }

        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            System.out.println(e.getMessage());
        }
    }

    public void sendMessage(Player p){
        try {
            if (oos != null){
                oos.writeObject(new Message(p.name +" -- (Player " + p.pid + ") joined the lobby", 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
