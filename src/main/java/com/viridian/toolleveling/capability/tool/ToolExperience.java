package com.viridian.toolleveling.capability.tool;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;

import java.util.Random;
public class ToolExperience implements INBTSerializable<CompoundTag> {
    private int experience;
    private int level;
    private int nextLevelExperience;
    private final ToolStats toolStats;
    private final ToolAbilities toolAbilities;

    private static final int BASE_XP = 15; // Base XP required for leveling up from level 1 to 2.
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double XP_MULTIPLIER = 1.75; // Multiplier for each subsequent level.

    public ToolExperience() {
        this.experience = 0;
        this.level = 1;
        this.nextLevelExperience = calculateNextLevelExperience(level);
        this.toolStats = new ToolStats();
        this.toolAbilities = new ToolAbilities();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Experience", experience);
        nbt.putInt("Level", level);
        nbt.putInt("NextLevelExperience", nextLevelExperience);
        nbt.put("ToolStats", toolStats.serializeNBT());
        nbt.put("ToolAbilities", toolAbilities.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.experience = nbt.getInt("Experience");
        this.level = nbt.getInt("Level");
        this.nextLevelExperience = nbt.contains("NextLevelExperience") ? nbt.getInt("NextLevelExperience") : calculateNextLevelExperience(this.level);
        this.toolStats.deserializeNBT(nbt.getCompound("ToolStats"));
        this.toolAbilities.deserializeNBT(nbt.getCompound("ToolAbilities"));
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

    public double getExperiencePercentage() {
        return (double) experience / calculateNextLevelExperience(this.level);
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

    public ToolStats getToolStats() {
        return toolStats;
    }

    public ToolAbilities getToolAbilities() { return toolAbilities; }

    public void checkLeveledUp(boolean hasLeveledUp, BlockEvent.BreakEvent event) {
        if (hasLeveledUp) {
            ((Level) event.getLevel()).playSound(null, event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            event.getPlayer().sendSystemMessage(Component.translatable("chat.tooltip.levelup", this.serializeNBT().getInt("Level")));
            increaseToolStats();
        }
    }

    public void increaseToolStats() {
        Random random = new Random();

        int statToIncrease = random.nextInt(2);

        switch (statToIncrease) {
            case 0 -> toolStats.setMiningSpeed(toolStats.getMiningSpeed() + 0.1f);
            case 1 -> toolStats.setFortuneLevel((toolStats.getFortuneLevel()) + 1);
            default -> throw new IllegalStateException("Unexpected value: " + statToIncrease);
        }
    }
}
