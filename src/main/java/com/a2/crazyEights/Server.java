package com.a2.crazyEights;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server implements Runnable {

    protected int          serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean      isStopped    = false;
    protected Thread       runningThread= null;

    protected ArrayList<WorkerRunnable> runnables = new ArrayList<>();
    protected GameState gameState = new GameState();

    public static void main(String[] args) {
        Server server = new Server(9200);
        System.out.println("SERVER RUNNING (port=9200)");
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
        while(gameState.getPlayers().size() < 4){
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
        gameState.addPlayer(p);
    }

    public void setIsReady(int i){
        gameState.setPlayerReady(i);
    }

    public void setGameState(GameState g){
        gameState = g;
    }
}

class WorkerRunnable implements Runnable{

    protected Socket clientSocket;
    protected final Server server;

    protected Player player = null;
    protected ObjectOutputStream oos = null;

    private volatile boolean mIsStopped = false;

    public WorkerRunnable(Socket clientSocket, Server s) {
        this.clientSocket = clientSocket;
        this.server = s;
    }

    public void run() {
        setStopped(false);

        try {
            InputStream input  = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();

            ObjectInputStream ois = new ObjectInputStream(input);
            ObjectOutputStream oos = new ObjectOutputStream(output);

            this.oos = oos;

            ArrayList<Player> players;
            ArrayList<WorkerRunnable> runnables;
            Object o;

            while(!mIsStopped){
                try {
                    // reset so object updates (weird java thing)
                    o = ois.readObject();
                    runnables = server.runnables;
                    boolean ready = false;

                    if (o instanceof Player){
                        //Update players
                        players = server.gameState.getPlayers();

                        Player newPlayer = (Player) o;

                        // Pregame lobby for new players and updated for current
                        if (!newPlayer.getReady()){
                            newPlayer.pid = players.size() + 1;

                            player = newPlayer;
                            server.addPlayer(newPlayer);

                            oos.writeObject(newPlayer);

                        } else {
                            server.setIsReady(newPlayer.pid - 1);
                            ready = true;
                        }

                        // Check if enough players to start game than start it
                        if (players.size() > 2 && server.gameState.isReady()){
                            for (Player p : players){
                                System.out.println("Player " + p.pid + " -- isReady=" + p.getReady());
                            }
                            // Start game
                            System.out.println("\nSTARTING GAME");
                            server.gameState.startGame();
                        }

                        // Game is now running and playing
                        if (server.gameState.isRunning()){

                            if (server.gameState.whoseTurn == -1){
                                server.gameState.setNextTurn();
                            }

                            for (WorkerRunnable r : runnables){
                                r.sendGameState();
                            }
                        }
                    }

                    // Pre game lobby messages
                    if (player != null && runnables.size() > 0 && !ready && !(o instanceof GameState)){
                        for (WorkerRunnable r : runnables){
                            if (r.player.pid != player.pid){
                                r.sendMessage(player);
                            }
                        }
                    }

                    // Pass game to appropriate player and update local game
                    if (o instanceof GameState){
                        GameState gs = (GameState) o;
                        server.setGameState(gs);

                        // Send game to next player
                        for (WorkerRunnable r : runnables){
                            if (r.player.pid == gs.whoseTurn){
                                r.sendGameState();
                            }
                        }

                        System.out.println("Whose next " + gs.whoseTurn);
                    }

                } catch (EOFException ex){
                    input.close();
                    oos.close();

                    System.out.println("Closing Socket for lost connection...");
                    setStopped(true);

                    clientSocket.close();
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
                int howManyNeeded = server.gameState.players.size();
                oos.writeObject(new Message( "(Player " + p.pid + ") joined the lobby", 1));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendGameState(){
        try {
            if (oos != null){
                oos.writeObject(server.gameState);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStopped(boolean isStop) {
        if (mIsStopped != isStop)
            mIsStopped = isStop;
    }
}
