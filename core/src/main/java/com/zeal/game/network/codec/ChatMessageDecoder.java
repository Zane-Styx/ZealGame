package com.zeal.game.network.codec;

import com.zeal.game.network.ChatMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes bytes written by {@link ChatMessageEncoder} back into ChatMessage.
 */
public class ChatMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // We need at least 4 bytes for sender length
        in.markReaderIndex();

        if (in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }

        int senderLen = in.readInt();
        if (senderLen < 0) {
            ctx.close();
            return;
        }

        if (in.readableBytes() < senderLen + 4 + 8) { // sender + contentLen + timestamp
            in.resetReaderIndex();
            return;
        }

        byte[] senderBytes = new byte[senderLen];
        if (senderLen > 0) in.readBytes(senderBytes);

        // content length
        int contentLen = in.readInt();
        if (contentLen < 0) {
            ctx.close();
            return;
        }

        if (in.readableBytes() < contentLen + 8) { // content + timestamp
            in.resetReaderIndex();
            return;
        }

        byte[] contentBytes = new byte[contentLen];
        if (contentLen > 0) in.readBytes(contentBytes);

        long timestamp = in.readLong();

        String sender = new String(senderBytes, StandardCharsets.UTF_8);
        String content = new String(contentBytes, StandardCharsets.UTF_8);

        out.add(new ChatMessage(sender, content) {
            // preserve timestamp by overriding getter (quick inline subclass)
            @Override
            public long getTimestamp() {
                return timestamp;
            }
        });
    }
}
