package com.viridian.toolleveling.capability.tool;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.slf4j.Logger;

public class ToolExperience implements INBTSerializable<CompoundTag> {
    private int experience;
    private int level;
    private int nextLevelExperience;
    private static final int BASE_XP = 15; // Base XP required for leveling up from level 1 to 2.
    private static final Logger LOGGER = LogUtils.getLogger();
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
        nbt.putInt("NextLevelExperience", nextLevelExperience);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.experience = nbt.getInt("Experience");
        this.level = nbt.getInt("Level");
        this.nextLevelExperience = nbt.contains("NextLevelExperience") ? nbt.getInt("NextLevelExperience") : calculateNextLevelExperience(this.level);
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
        LOGGER.info("nextLevelExp: " + nextLevelExperience);
        LOGGER.info("experience: " + experience);

        while (experience >= nextLevelExperience) {
            experience -= nextLevelExperience;
            level++;
            nextLevelExperience = calculateNextLevelExperience(level);
            didLevelUp = true;
        }

        return didLevelUp;
    }

    public int calculateNextLevelExperience(int level) {
        if (level == 1) {
            return BASE_XP;
        } else {
            return (int) (Math.pow(XP_MULTIPLIER, level - 1) * BASE_XP);
        }
    }

    public String buildExpBar() {
        double experiencePercentage = (double) experience / calculateNextLevelExperience(this.level);
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
