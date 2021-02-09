package com.a2.crazyEights;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 234L;

    String name;
    int pid;

    String[] cards = new String[7];

    Player(String n){
        name = n;
    }

    public void print(){
        System.out.println(name);
    }
}
