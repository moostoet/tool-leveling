package com.viridian.toolleveling.capability.tool;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ToolStats implements INBTSerializable<CompoundTag> {
    private float miningSpeed;
    private int fortuneLevel;

    public ToolStats() {
        this.miningSpeed = 0.0f;
        this.fortuneLevel = 0;
    }

    public float getMiningSpeed() {
        return this.miningSpeed;
    }

    public void setMiningSpeed(float miningSpeed) {
        this.miningSpeed = miningSpeed;
    }

    public int getFortuneLevel() {
        return fortuneLevel;
    }

    public void setFortuneLevel(int fortuneLevel) {
        this.fortuneLevel = fortuneLevel;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("MiningSpeed", miningSpeed);
        nbt.putInt("FortuneLevel", fortuneLevel);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.miningSpeed = nbt.getFloat("MiningSpeed");
        this.fortuneLevel = nbt.getInt("FortuneLevel");
    }
}
