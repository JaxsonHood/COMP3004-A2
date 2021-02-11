package com.a2.crazyEights;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest extends TestCase {
    Game game = new Game();
    Server s;

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

    public void testCreatePlayer() {
        assertEquals("Jaxson", "J");
    }

    public void testStartServer(){
        s = new Server(9200);
    }
}
