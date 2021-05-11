package io.socket.engineio.parser;

import java.util.*;

@SuppressWarnings("unchecked")
public interface Parser {

    Map<String, Integer> PACKETS = Collections.unmodifiableMap(new HashMap<String, Integer>() {{
        put(Packet.OPEN, 0);
        put(Packet.CLOSE, 1);
        put(Packet.PING, 2);
        put(Packet.PONG, 3);
        put(Packet.MESSAGE, 4);
        put(Packet.UPGRADE, 5);
        put(Packet.NOOP, 6);
    }});
    Map<Integer, String> PACKETS_REVERSE = Collections.unmodifiableMap(new HashMap<Integer, String>() {{
        put(0, Packet.OPEN);
        put(1, Packet.CLOSE);
        put(2, Packet.PING);
        put(3, Packet.PONG);
        put(4, Packet.MESSAGE);
        put(5, Packet.UPGRADE);
        put(6, Packet.NOOP);
    }});

    Packet<String> ERROR_PACKET = new Packet<>(Packet.ERROR, "parser error");

    Parser PROTOCOL_V3 = new ParserV3();
    Parser PROTOCOL_V4 = new ParserV4();

    interface EncodeCallback<T> {
        void call(T data);
    }

    interface DecodePayloadCallback<T> {
        boolean call(Packet<T> packet, int index, int total);
    }

    Packet<?> decodePacket(Object data);

    void encodePacket(Packet<?> packet, boolean supportsBinary, EncodeCallback<Object> callback);

    void encodePayload(List<Packet<?>> packets, boolean supportsBinary, EncodeCallback<Object> callback);
    void decodePayload(Object data, DecodePayloadCallback<Object> callback);

    static byte[] concatBuffer(byte[] ...arrays) {
        int length = 0;
        for (byte[] item : arrays) {
            length += item.length;
        }

        final byte[] result = new byte[length];
        int idx = 0;
        for (byte[] item : arrays) {
            System.arraycopy(item, 0, result, idx, item.length);
            idx += item.length;
        }

        return result;
    }

}