package com.a2.crazyEights;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    public void testGame(){
        // Create new server and start it
        Server server = new Server(9200);
        System.out.println("SERVER RUNNING (port=9200)");
        new Thread(server).start();

        // Create new game and connect it to server
        Game g = new Game();
        RunGame rg = new RunGame(g);

        new Thread(rg).start();
        rg.readyUp();

    }
}

class RunGame implements Runnable {
    private final Game game;

    public RunGame(Game g){
        this.game = g;
    }

    public void run() {
        game.connectSocket();
    }

    public void readyUp(){
        game.readyUp();
    }
}
