package com.viridian.toolleveling.networking;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class ToolLevelingNetwork {
    private static final String PROTOCOL_VERSION = "1.0.0";

    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("toolleveling", "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerMessages() {
        NETWORK.registerMessage(0,
                TestMessage.class,
                TestMessage::encode,
                TestMessage::new,
                TestMessage::handle);
    }
}
