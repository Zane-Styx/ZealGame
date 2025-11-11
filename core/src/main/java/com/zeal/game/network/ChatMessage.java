package com.zeal.game.network;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String sender;
    private final String content;
    private final long timestamp;

    public ChatMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s]: %s", sender, content);
    }
}