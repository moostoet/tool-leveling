package com.viridian.toolleveling.entities;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class HighlightEntity extends Entity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final EntityDataAccessor<Integer> PERSISTENT_COLOR = SynchedEntityData.defineId(HighlightEntity.class, EntityDataSerializers.INT);

    private int ttl = 60;

    public HighlightEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(PERSISTENT_COLOR, 0);
    }

    public static EntityDataAccessor<Integer> getPersistentColor() {
        return PERSISTENT_COLOR;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        this.ttl = nbt.getInt("TTL");
        this.entityData.set(PERSISTENT_COLOR, nbt.getInt("color"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        nbt.putInt("TTL", this.ttl);
        nbt.putInt("color", this.entityData.get(PERSISTENT_COLOR));
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
