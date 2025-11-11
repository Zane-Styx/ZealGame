package com.zeal.game.network.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import com.zeal.game.network.ChatMessage;

import java.util.logging.Logger;

public class GameServerHandler extends SimpleChannelInboundHandler<ChatMessage> {
    private static final Logger logger = Logger.getLogger(GameServerHandler.class.getName());
    private final GameServer gameServer;

    public GameServerHandler(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatMessage msg) {
        logger.info("Received message: " + msg);
        gameServer.broadcast(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String address = channel.remoteAddress().toString();
        gameServer.registerClient(channel, "Player-" + address);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        gameServer.removeClient(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warning("Exception caught: " + cause.getMessage());
        ctx.close();
    }
}