package com.viridian.toolleveling.capability.tool;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ToolExperience implements INBTSerializable<CompoundTag> {
    private int experience;
    private int level;

    public ToolExperience() {
        this.experience = 0;
        this.level = 1;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Experience", experience);
        nbt.putInt("Level", level);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.experience = nbt.getInt("Experience");
        this.level = nbt.getInt("Level");
    }
}
