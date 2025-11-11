package com.zeal.game.network;

public class NetworkConstants {
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_HOST = "localhost";
    
    // Network protocol constants
    public static final byte CHAT_MESSAGE = 0x01;
    
    private NetworkConstants() {
        // Prevent instantiation
    }
}