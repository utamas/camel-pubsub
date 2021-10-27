package com.pkb.sandbox.pubsub;

import java.io.Serializable;

public class Event implements Serializable {
    private String id;
    private String message;

    public Event() {
    }

    public Event(String id, String message) {
        this.id = id;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
