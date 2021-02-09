package com.a2.crazyEights;

import java.io.Serializable;

public class Game implements Serializable {
    private static final long serialVersionUID = 123L;

    public void createPlayer(){
        Player p = new Player("Jaxson Hood");
        p.print();
    }
}
