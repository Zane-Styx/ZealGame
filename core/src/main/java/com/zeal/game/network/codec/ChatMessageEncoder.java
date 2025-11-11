package com.zeal.game.network.codec;

import com.zeal.game.network.ChatMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * Encodes ChatMessage into a simple binary format:
 * [senderLen:int][sender:bytes][contentLen:int][content:bytes][timestamp:long]
 */
public class ChatMessageEncoder extends MessageToByteEncoder<ChatMessage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ChatMessage msg, ByteBuf out) throws Exception {
        byte[] sender = msg.getSender() != null ? msg.getSender().getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] content = msg.getContent() != null ? msg.getContent().getBytes(StandardCharsets.UTF_8) : new byte[0];

        out.writeInt(sender.length);
        if (sender.length > 0) out.writeBytes(sender);

        out.writeInt(content.length);
        if (content.length > 0) out.writeBytes(content);

        out.writeLong(msg.getTimestamp());
    }
}
