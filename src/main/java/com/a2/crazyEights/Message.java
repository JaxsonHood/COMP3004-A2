package com.a2.crazyEights;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 234L;

    public final String message;
    public final int pid;

    Message(String message, int pid){
        this.message = message;
        this.pid = pid;
    }
}
