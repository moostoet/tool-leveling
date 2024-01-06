package com.viridian.toolleveling.entities;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class HighlightEntity extends Entity  {
    private static final Logger LOGGER = LogUtils.getLogger();

    private int ttl = 100;

    public HighlightEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        LOGGER.info("Spawned with data: " + getEntityData());
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("TTL", this.ttl);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        this.ttl = nbt.getInt("TTL");
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ttl-- <= 0) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean hasCustomOutlineRendering(Player player) {
        return true;
    }
}
