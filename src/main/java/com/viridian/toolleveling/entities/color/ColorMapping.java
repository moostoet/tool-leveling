package com.viridian.toolleveling.entities.color;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class ColorMapping {
    private static final Map<Block, Integer> blockColors = new HashMap<>();

    static {
        blockColors.put(Blocks.COAL_ORE, packColor(125, 125, 125, 255));
        blockColors.put(Blocks.DEEPSLATE_COAL_ORE, packColor(125, 125, 125, 255));

        blockColors.put(Blocks.DIAMOND_ORE, packColor(0, 255, 255, 255)); // turquoise
        blockColors.put(Blocks.DEEPSLATE_DIAMOND_ORE, packColor(0, 255, 255, 255)); // turquoise

        blockColors.put(Blocks.IRON_ORE, packColor(205, 185, 150, 255));
        blockColors.put(Blocks.DEEPSLATE_IRON_ORE, packColor(205, 185, 150, 255));

        blockColors.put(Blocks.GOLD_ORE, packColor(255, 215, 0, 255)); // gold color
        blockColors.put(Blocks.DEEPSLATE_GOLD_ORE, packColor(255, 215, 0, 255)); // gold color

        blockColors.put(Blocks.NETHER_GOLD_ORE, packColor(255, 215, 0, 255)); // gold color

        blockColors.put(Blocks.REDSTONE_ORE, packColor(255, 0, 0, 255));
        blockColors.put(Blocks.DEEPSLATE_REDSTONE_ORE, packColor(255, 0, 0, 255));

        blockColors.put(Blocks.LAPIS_ORE, packColor(90, 90, 255, 255));
        blockColors.put(Blocks.DEEPSLATE_LAPIS_ORE, packColor(90, 90, 255, 255));

        blockColors.put(Blocks.EMERALD_ORE, packColor(50, 220, 50, 255));
        blockColors.put(Blocks.DEEPSLATE_EMERALD_ORE, packColor(50, 220, 50, 255));

        blockColors.put(Blocks.COPPER_ORE, packColor(184, 115, 51, 255));
        blockColors.put(Blocks.DEEPSLATE_COPPER_ORE, packColor(184, 115, 51, 255));

        blockColors.put(Blocks.NETHER_QUARTZ_ORE, packColor(232, 220, 202, 255));
    }

    public static int packColor(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static Integer getColorForBlock(Block block) {
        return blockColors.get(block);
    }
}
