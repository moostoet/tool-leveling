package com.viridian.toolleveling.capability.tool;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ToolExperience implements INBTSerializable<CompoundTag> {
    private int experience;
    private int level;
    private static final int BASE_XP = 15; // Base XP required for leveling up from level 1 to 2.
    private static final double XP_MULTIPLIER = 1.75; // Multiplier for each subsequent level.

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

    /**
     * Calculates the total experience required to reach the next level.
     * @return the experience required to level up
     */
    public int getExperienceForNextLevel() {
        if (level == 1) {
            return BASE_XP;
        } else {
            return (int) (Math.pow(XP_MULTIPLIER, level - 1) * BASE_XP);
        }
    }

    /**
     * Method to add experience and handle leveling up.
     * @param additionalExp The additional experience to add.
     */
    public void addExperience(int additionalExp) {
        experience += additionalExp;

        while (experience >= getExperienceForNextLevel()) {
            experience -= getExperienceForNextLevel();
            level++;
        }
    }
}
