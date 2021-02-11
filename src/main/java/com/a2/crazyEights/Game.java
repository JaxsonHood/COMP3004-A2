package com.a2.crazyEights;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Game implements Serializable {
    private static final long serialVersionUID = 2L;

    private int playerPid = -1;

    public static void main(String[] args) throws ClassNotFoundException{
        Game g = new Game();
        g.connectSocket();
    }

    Game(){
        System.out.println("Welcome to CrazyEights");
    }

    public void connectSocket() throws ClassNotFoundException{
        String hostname = "localhost";
        int port = 9200;

        try (Socket socket = new Socket(hostname, port)) {

            Scanner scanner = new Scanner(System.in);  // Create a Scanner object
            System.out.println("Enter Your Name: ");
            String name = scanner.nextLine();  // Read user input

            // get the output stream from the socket.
            OutputStream outputStream = socket.getOutputStream();
            // create an object output stream from the output stream so we can send an object through it
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            // Send the object
            objectOutputStream.writeObject(new Player(name));

            // get the input stream from the connected socket
            InputStream inputStream = socket.getInputStream();
            // create a DataInputStream so we can read data from it.
            ObjectInputStream ois = new ObjectInputStream(inputStream);

             //Keep checking for new data
            while (true) {
                Object o = ois.readObject();

                if (o instanceof Player){
                    // read the player data from socket and set pid
                    Player p = (Player) o;
                    if (this.playerPid == -1 && !p.name.equals("null")){
                        this.playerPid = p.pid;
                        System.out.println("You are Player " + this.playerPid);

                    } else if (this.playerPid != -1 && !p.name.equals(name)) {
                        System.out.println(p.name + " joined as Player " + p.pid);

                    } else { System.out.println("Sorry, game is full! You cannot join."); }
                }

                if (o instanceof Message){
                    Message m = (Message) o;
                    System.out.println(m.message);
                }
            }


        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
}
