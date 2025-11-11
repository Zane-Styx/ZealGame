package com.zeal.game.network.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.zeal.game.network.ChatMessage;

import java.util.logging.Logger;

public class GameClientHandler extends SimpleChannelInboundHandler<ChatMessage> {
    private static final Logger logger = Logger.getLogger(GameClientHandler.class.getName());
    private final GameClient gameClient;

    public GameClientHandler(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessage msg) {
        logger.info("Received message: " + msg);
        gameClient.handleMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warning("Exception caught: " + cause.getMessage());
        ctx.close();
    }
}