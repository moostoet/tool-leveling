package com.viridian.toolleveling.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public interface IPacket {
    public void write(FriendlyByteBuf buffer);
    public void read(FriendlyByteBuf buffer);

    public void process(Player player);
}
