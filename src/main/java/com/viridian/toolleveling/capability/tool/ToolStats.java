package com.viridian.toolleveling.capability.tool;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ToolStats implements INBTSerializable<CompoundTag> {
    private float miningSpeed;
    private int harvestLevel;
    private int fortuneLevel;

    public ToolStats(float miningSpeed, int harvestLevel, int fortuneLevel) {
        this.miningSpeed = miningSpeed;
        this.harvestLevel = harvestLevel;
        this.fortuneLevel = fortuneLevel;
    }

    public float getMiningSpeed() {
        return this.miningSpeed;
    }

    public void setMiningSpeed(float miningSpeed) {
        this.miningSpeed = miningSpeed;
    }

    public int getHarvestLevel() {
        return harvestLevel;
    }

    public void setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
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
        nbt.putInt("HarvestLevel", harvestLevel);
        nbt.putInt("FortuneLevel", fortuneLevel);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.miningSpeed = nbt.getFloat("MiningSpeed");
        this.harvestLevel = nbt.getInt("HarvestLevel");
        this.fortuneLevel = nbt.getInt("FortuneLevel");
    }
}
