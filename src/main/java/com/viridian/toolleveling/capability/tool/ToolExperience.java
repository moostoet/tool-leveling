package com.viridian.toolleveling.capability.tool;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ToolExperience implements INBTSerializable<CompoundTag> {
    private int experience;
    private int level;
    private int nextLevelExperience;
    private static final int BASE_XP = 15; // Base XP required for leveling up from level 1 to 2.
    private static final double XP_MULTIPLIER = 1.75; // Multiplier for each subsequent level.

    public ToolExperience() {
        this.experience = 0;
        this.level = 1;
        this.nextLevelExperience = calculateNextLevelExperience(level);
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
     * Method to add experience and handle leveling up.
     *
     * @param additionalExp The additional experience to add.
     * @return didLevelUp If the player leveled up.
     */
    public boolean addExperience(int additionalExp) {
        boolean didLevelUp = false;
        experience += additionalExp;

        while (experience >= nextLevelExperience) {
            experience -= nextLevelExperience;
            level++;
            nextLevelExperience = calculateNextLevelExperience(level);
            didLevelUp = true;
        }

        return didLevelUp;
    }

    private int calculateNextLevelExperience(int level) {
        if (level == 1) {
            return BASE_XP;
        } else {
            return (int) (Math.pow(XP_MULTIPLIER, level - 1) * BASE_XP);
        }
    }

    public int getNextLevelExperience() {
        return nextLevelExperience;
    }

    public String buildExpBar() {
        double experiencePercentage = (double) experience / getNextLevelExperience();
        StringBuilder expBar = new StringBuilder("§f[");
        for (int i = 1; i <= 10; i++) {
            if ((experiencePercentage * 10) >= i) {
                expBar.append("§6█");
            } else {
                expBar.append("§f█");
            }
        }
        expBar.append("§f]");

        return expBar.toString();
    }
}
