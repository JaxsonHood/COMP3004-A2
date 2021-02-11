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
    protected ArrayList<Message> messages = new ArrayList<>();
    protected ArrayList<WorkerRunnable> runnables = new ArrayList<>();

    public static void main(String[] args) {
        Server server = new Server(9200);
        new Thread(server).start();
    }

    public Server(int port){
        this.serverPort = port;
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

            while(true){
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
        System.out.println("MESSAGE TO Player " + player.pid + " --- FROM Player " + p.pid);
        try {
            if (oos != null){
                oos.writeObject(new Message("TEST", 1, 2));
            }

        } catch (IOException e) {
            //report exception somewhere.
            e.printStackTrace();
        }
    }
}

//public class Server implements Serializable {
//    private static final long serialVersionUID = 2L;
//
//    public static void main(String[] args) {
//        Server s = new Server();
//        s.openSocket();
//    }
//
//    // Store all the cards in the deck
//    protected ArrayList<Card> allCards = new ArrayList<>();
//
//    // Store all the players in the game
//    protected ArrayList<Player> allPlayers = new ArrayList<>();
//
//    // Store streams
//    private final ArrayList<ObjectInputStream> inputStreams = new ArrayList<>();
//    private final ArrayList<ObjectOutputStream> outputStreams = new ArrayList<>();
//
//    public String[] suits = {"S", "H", "D", "C"};
//    public String[] ranks = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
//
//    Server() {
//        this.populateDeck();
//        System.out.println("Deck Size: " + this.allCards.size());
//    }
//
//    public void openSocket(){
//        int port = 9200;
//
//        try (ServerSocket serverSocket = new ServerSocket(port)) {
//
//            System.out.println("Server is listening on port " + port);
//
//            while (true) {
//                Socket socket = serverSocket.accept();
//
//                // get the input stream from the connected socket
//                InputStream inputStream = socket.getInputStream();
//                ObjectInputStream ois = new ObjectInputStream(inputStream);
//
//                // get the output stream from the socket.
//                OutputStream outputStream = socket.getOutputStream();
//                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
//
//                this.inputStreams.add(ois);
//                this.outputStreams.add(oos);
//
//                new Thread(() -> {
//                    // code goes here.
//                    int threadNum = inputStreams.size();
//
//                    try {
//                        Object o = this.inputStreams.get(threadNum - 1).readObject();
//
//                        if (o instanceof Player){
//                            // read the player data from socket
//                            Player newPlayer = (Player) o;
//
//                            if (newPlayer.pid == -1 && this.allPlayers.size() < 4){
//                                this.addNewPlayer(newPlayer);
//                                System.out.println("How many players: " + this.allPlayers.size());
//
//                                for (ObjectOutputStream outs : this.outputStreams) {
//                                    outs.writeObject(this.allPlayers.get(this.allPlayers.size() - 1));
//                                }
//                            } else {
//                                // Send the updated player object
//                                this.outputStreams.get(threadNum - 1).writeObject(new Player("null"));
//                            }
//                        }
//
//                    } catch (IOException ex){
//                        System.out.println("I/O error: " + ex.getMessage());
//                    } catch (ClassNotFoundException cnf){
//                        System.out.println("Class Not Found: " + cnf.getMessage());
//                    }
//
//                }).start();
//            }
//
//        } catch (IOException ex) {
//            System.out.println("Server exception: " + ex.getMessage());
//            ex.printStackTrace();
//        }
//    }
//
//    public void addNewPlayer(Player p) throws IOException {
//        // Set player pid
//        p.pid = allPlayers.size() + 1;
//
//        // Add player to players list
//        allPlayers.add(p);
//        System.out.println("NEW PLAYER ADDED --  # of Players: " + allPlayers.size());
//    }
//
//    public void populateDeck(){
//        // Clear deck in case cards left over
//        allCards.clear();
//
//        /* Creating all possible cards... */
//        for (String s : this.suits) {
//            for (String r : this.ranks) {
//                allCards.add(new Card(r, s));
//            }
//        }
//    }
//}
