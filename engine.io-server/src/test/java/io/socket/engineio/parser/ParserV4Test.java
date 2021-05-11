package io.socket.engineio.parser;

import io.socket.engineio.TestUtils;
import org.json.JSONArray;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test {@link ParserV4} against reference JS implementation.
 */
public final class ParserV4Test {

    /**
     * This is just to remove the warning on the field.
     */
    @Test
    public void testProtocolVersion() {
        assertEquals(4, ParserV4.PROTOCOL);
    }

    @Test
    public void testEncodePacket_string() {
        final Packet<String> packet = new Packet<>(Packet.MESSAGE);

        packet.data = "Hello World";
        Parser.PROTOCOL_V4.encodePacket(packet, false, data -> {
            String result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePacket_string.js", packet.data, String.class);
            assertEquals(result, data);
        });

        packet.data = "Engine.IO";
        Parser.PROTOCOL_V4.encodePacket(packet, false, data -> {
            String result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePacket_string.js", packet.data, String.class);
            assertEquals(result, data);
        });
    }

    @Test
    public void testEncodePacket_binary() {
        final Packet<byte[]> packet = new Packet<>(Packet.MESSAGE);

        packet.data = new byte[] { 1, 2, 3, 4, 5 };
        Parser.PROTOCOL_V4.encodePacket(packet, true, data -> {
            byte[] result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePacket_binary.js", packet.data, byte[].class);
            assertArrayEquals(result, (byte[]) data);
        });
    }

    @Test
    public void testEncodePacket_base64() {
        final Packet<byte[]> packet = new Packet<>(Packet.MESSAGE);

        packet.data = new byte[] { 1, 2, 3, 4, 5 };
        Parser.PROTOCOL_V4.encodePacket(packet, false, data -> {
            String result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePacket_base64.js", packet.data, String.class);
            assertEquals(result, data);
        });
    }

    @Test
    public void testEncodePayload_string_empty() {
        final List<Packet<?>> packets = new ArrayList<>();
        final JSONArray jsonArray = new JSONArray();

        Parser.PROTOCOL_V4.encodePayload(packets, false, data -> {
            String result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePayload_string.js", jsonArray.toString(), String.class);
            assertEquals(result, data);
        });
    }

    @Test
    public void testEncodePayload_string() {
        final String[] messages = new String[] {
                "Hello World",
                "Engine.IO"
        };
        final List<Packet<?>> packets = new ArrayList<>();
        final JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < messages.length; i++) {
            jsonArray.put(i, messages[i]);

            Packet<String> packet = new Packet<>(Packet.MESSAGE);
            packet.data = messages[i];
            packets.add(packet);
        }

        Parser.PROTOCOL_V4.encodePayload(packets, true, data -> {
            String result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePayload_string.js", jsonArray.toString(), String.class);
            assertEquals(result, data);
        });
    }

    @Test
    public void testEncodePayload_base64() {
        final byte[][] messages = new byte[][] {
                {1, 2, 3, 4, 5}, {11, 12, 13, 14, 15}
        };
        final List<Packet<?>> packets = new ArrayList<>();
        final JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < messages.length; i++) {
            jsonArray.put(i, messages[i]);

            Packet<byte[]> packet = new Packet<>(Packet.MESSAGE);
            packet.data = messages[i];
            packets.add(packet);
        }

        Parser.PROTOCOL_V4.encodePayload(packets, true, data -> {
            String result = TestUtils.runScriptAndGetOutput("src/test/resources/testEncodePayload_base64.js", jsonArray.toString(), String.class);
            assertEquals(result, data);
        });
    }

    @Test
    public void testDecodePacket_null() {
        Packet<?> packet = Parser.PROTOCOL_V4.decodePacket(null);
        assertNotNull(packet);
        assertEquals(Packet.ERROR, packet.type);
        assertEquals("parser error", packet.data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodePacket_error() {
        // Pass an int to get exception
        Parser.PROTOCOL_V4.decodePacket(0);
    }

    @Test
    public void testDecodePacket_string() {
        final Packet<String> packetOriginal = new Packet<>(Packet.MESSAGE, "Engine.IO");
        Parser.PROTOCOL_V4.encodePacket(packetOriginal, true, data -> {
            Packet<?> packetDecoded = Parser.PROTOCOL_V4.decodePacket(data);
            assertEquals(Packet.MESSAGE, packetDecoded.type);
            assertEquals(String.class, packetDecoded.data.getClass());
            assertEquals(packetOriginal.data, packetDecoded.data);
        });
    }

    @Test
    public void testDecodePacket_binary() {
        final Packet<byte[]> packetOriginal = new Packet<>(Packet.MESSAGE, "Engine.IO".getBytes(StandardCharsets.UTF_8));
        Parser.PROTOCOL_V4.encodePacket(packetOriginal, true, data -> {
            Packet<?> packetDecoded = Parser.PROTOCOL_V4.decodePacket(data);
            assertEquals(Packet.MESSAGE, packetDecoded.type);
            assertEquals(byte[].class, packetDecoded.data.getClass());
            assertArrayEquals(packetOriginal.data, (byte[]) packetDecoded.data);
        });
    }

    @Test
    public void testDecodePacket_base64() {
        final Packet<byte[]> packetOriginal = new Packet<>(Packet.MESSAGE, "Engine.IO".getBytes(StandardCharsets.UTF_8));
        Parser.PROTOCOL_V4.encodePacket(packetOriginal, true, data -> {
            Packet<?> packetDecoded = Parser.PROTOCOL_V4.decodePacket(data);
            assertEquals(Packet.MESSAGE, packetDecoded.type);
            assertEquals(byte[].class, packetDecoded.data.getClass());
            assertArrayEquals(packetOriginal.data, (byte[]) packetDecoded.data);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodePayload_error() {
        Parser.PROTOCOL_V4.decodePayload("abcxyz", (packet, index, total) -> false);
    }

    @Test
    public void testDecodePayload_string() {
        final List<Packet<?>> packets = new ArrayList<>();
        packets.add(new Packet<>(Packet.MESSAGE, "Engine.IO"));
        packets.add(new Packet<>(Packet.MESSAGE, "Test.Data"));

        Parser.PROTOCOL_V4.encodePayload(packets, false, data -> {
            assertEquals(String.class, data.getClass());

            Parser.PROTOCOL_V4.decodePayload(data, (packet, index, total) -> {
                Packet<?> originalPacket = packets.get(index);
                assertEquals(originalPacket.data.getClass(), packet.data.getClass());
                assertEquals(originalPacket.type, packet.type);
                assertEquals(originalPacket.data, packet.data);

                return true;
            });
        });
    }

    @Test
    public void testDecodePayload_base64() {
        final List<Packet<?>> packets = new ArrayList<>();
        packets.add(new Packet<>(Packet.MESSAGE, "Engine.IO".getBytes(StandardCharsets.UTF_8)));
        packets.add(new Packet<>(Packet.MESSAGE, "Test.Data".getBytes(StandardCharsets.UTF_8)));

        Parser.PROTOCOL_V4.encodePayload(packets, false, data -> {
            assertEquals(String.class, data.getClass());

            Parser.PROTOCOL_V4.decodePayload(data, (packet, index, total) -> {
                Packet<?> originalPacket = packets.get(index);
                assertEquals(originalPacket.data.getClass(), packet.data.getClass());
                assertEquals(originalPacket.type, packet.type);
                assertArrayEquals((byte[]) originalPacket.data, (byte[]) packet.data);

                return true;
            });
        });
    }

    @Test
    public void testDecodePayload_exit() {
        final List<Packet<?>> packets = new ArrayList<>();
        packets.add(new Packet<>(Packet.MESSAGE, "Engine.IO"));
        packets.add(new Packet<>(Packet.MESSAGE, "Test.Data"));

        Parser.PROTOCOL_V4.encodePayload(packets, true, data -> {
            assertEquals(String.class, data.getClass());

            Parser.PROTOCOL_V4.decodePayload(data, (packet, index, total) -> {
                assertEquals(0, index);

                Packet<?> originalPacket = packets.get(index);
                assertEquals(originalPacket.data.getClass(), packet.data.getClass());
                assertEquals(originalPacket.type, packet.type);
                assertEquals(originalPacket.data, packet.data);

                return false;
            });
        });
    }
}