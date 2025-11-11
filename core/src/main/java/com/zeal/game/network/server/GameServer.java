package com.zeal.game.network.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
// Using custom codec - no deprecated Netty object codec imports required
import com.zeal.game.network.NetworkConstants;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import com.zeal.game.network.ChatMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class GameServer {
    private static final Logger logger = Logger.getLogger(GameServer.class.getName());
    private final int port;
    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final ConcurrentHashMap<Channel, String> connectedClients = new ConcurrentHashMap<>();

    public GameServer(int port) {
        this.port = port;
    }

    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                // inbound: frame decoder -> message decoder -> handler
                                new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4),
                                new com.zeal.game.network.codec.ChatMessageDecoder(),
                                new GameServerHandler(GameServer.this),
                                // outbound: encoder then length prepender (prepender added before encoder so encoder runs first on outbound)
                                new LengthFieldPrepender(4),
                                new com.zeal.game.network.codec.ChatMessageEncoder()
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            serverChannel = bootstrap.bind(port).sync().channel();
            logger.info("Server started on port " + port);
        } catch (Exception e) {
            logger.severe("Failed to start server: " + e.getMessage());
            shutdown();
        }
    }

    public void broadcast(ChatMessage message) {
        if (message == null) return;
        
        connectedClients.keySet().forEach(channel -> {
            if (channel.isActive()) {
                channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                    if (!future.isSuccess()) {
                        logger.warning("Failed to send message to " + channel.remoteAddress());
                    }
                });
            }
        });
    }

    public void shutdown() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    public void registerClient(Channel channel, String username) {
        connectedClients.put(channel, username);
        logger.info("Client connected: " + username);
        broadcast(new ChatMessage("Server", username + " joined the game"));
    }

    public void removeClient(Channel channel) {
        String username = connectedClients.remove(channel);
        if (username != null) {
            logger.info("Client disconnected: " + username);
            broadcast(new ChatMessage("Server", username + " left the game"));
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(NetworkConstants.DEFAULT_PORT);
        server.start();
    }
}