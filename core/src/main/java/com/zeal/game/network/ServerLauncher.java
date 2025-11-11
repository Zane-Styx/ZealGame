package com.zeal.game.network;

import com.zeal.game.network.server.GameServer;

public class ServerLauncher {
    public static void main(String[] args) {
        GameServer server = new GameServer(NetworkConstants.DEFAULT_PORT);
        server.start();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        
        System.out.println("Server started on port " + NetworkConstants.DEFAULT_PORT);
        System.out.println("Press Ctrl+C to stop the server");
    }
}