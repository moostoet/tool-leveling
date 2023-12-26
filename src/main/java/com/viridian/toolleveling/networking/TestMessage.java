package com.viridian.toolleveling.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

public class TestMessage {
    private final String data;

    // Construct message from provided data
    public TestMessage(String data) {
        this.data = data;
    }

    // Construct message by decoding from buffer
    public TestMessage(FriendlyByteBuf buffer) {
        this.data = buffer.readUtf(256);
    }

    // Encode the message to buffer
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.data);
    }

    // Handle received message
    public static void handle(TestMessage message, NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            // Your logic here. Executed on the main thread.
            System.out.println("Test message received: " + message.data);
        });
        context.setPacketHandled(true);
    }
}