package com.zeal.game.network.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.zeal.game.network.codec.ChatMessageDecoder;
import com.zeal.game.network.codec.ChatMessageEncoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import com.zeal.game.network.ChatMessage;
import com.zeal.game.network.NetworkConstants;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class GameClient {
    private static final Logger logger = Logger.getLogger(GameClient.class.getName());
    
    private final String host;
    private final int port;
    private Channel clientChannel;
    private EventLoopGroup group;
    private final String username;
    private ChatMessageListener messageListener;

    public GameClient(String username) {
        this(NetworkConstants.DEFAULT_HOST, NetworkConstants.DEFAULT_PORT, username);
    }

    public GameClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(
                                // inbound framing -> message decoder -> handler
                                new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4),
                                new ChatMessageDecoder(),
                                new GameClientHandler(GameClient.this),
                                // outbound encoder and length prepender (added after handler so outbound order is encoder then prepender)
                                new LengthFieldPrepender(4),
                                new ChatMessageEncoder()
                            );
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channelFuture.addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    clientChannel = f.channel();
                    logger.info("Connected to server " + host + ":" + port);
                    future.complete(null);
                } else {
                    logger.severe("Failed to connect to server: " + f.cause().getMessage());
                    future.completeExceptionally(f.cause());
                }
            });
        } catch (Exception e) {
            logger.severe("Failed to initialize client: " + e.getMessage());
            future.completeExceptionally(e);
        }

        return future;
    }

    public void sendMessage(String message) {
        if (clientChannel != null && clientChannel.isActive()) {
            ChatMessage chatMessage = new ChatMessage(username, message);
            clientChannel.writeAndFlush(chatMessage).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    logger.warning("Failed to send message: " + future.cause().getMessage());
                }
            });
        }
    }

    public void disconnect() {
        if (clientChannel != null) {
            clientChannel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public void setMessageListener(ChatMessageListener listener) {
        this.messageListener = listener;
    }

    void handleMessage(ChatMessage message) {
        if (messageListener != null) {
            messageListener.onMessageReceived(message);
        }
    }

    public interface ChatMessageListener {
        void onMessageReceived(ChatMessage message);
    }
}