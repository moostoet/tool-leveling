package com.viridian.toolleveling.capability.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.viridian.toolleveling.attachment.AttachmentTypes.TOOL_EXP;

public class ToolAbilities {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolAbilities.class);
    private final Set<ToolAbility> abilities;

    public ToolAbilities() {
        this.abilities = EnumSet.noneOf(ToolAbility.class);
    }

    public void addAbility(ToolAbility ability) {
        abilities.add(ability);
    }

    public void removeAbility(ToolAbility ability) {
        abilities.remove(ability);
    }

    public boolean hasAbility(ToolAbility ability) {
        return abilities.contains(ability);
    }

    public Set<ToolAbility> getAbilities() {
        return abilities;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag abilitiesList = new ListTag();
        for (ToolAbility ability : abilities) {
            abilitiesList.add(StringTag.valueOf(ability.name()));
        }
        tag.put("Abilities", abilitiesList);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        ListTag abilitiesList = tag.getList("Abilities", StringTag.TAG_STRING);
        this.abilities.clear();
        for (int i = 0; i < abilitiesList.size(); i++) {
            String name = abilitiesList.getString(i);
            try {
                abilities.add(ToolAbility.valueOf(name));
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Unknown ability '{}' found in NBT data, it will be ignored.", name);
            }
        }
    }

    public void applyAbilitiesOnBlockBreak(BlockEvent.BreakEvent event) {
        LOGGER.info(event.toString());
    }

    public void renderToolTips(ItemTooltipEvent event) {
        if (!abilities.isEmpty()) {
            int index = 6;
            event.getToolTip().add(index, Component.empty());
            for (ToolAbility ability : abilities) {
                index++;
                event.getToolTip().add(index, ability.getDisplayComponent());
            }
        }
    }

    public void handleRightClickAbilities(PlayerInteractEvent.RightClickBlock event) {
        if (this.hasAbility(ToolAbility.MAGMA_ABSORPTION)) handleMagmaAbsorptionAbility(event);
    }

    public void handleMagmaAbsorptionAbility(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();

        BlockHitResult result = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockState blockState = level.getBlockState(result.getBlockPos());

        if (blockState.getBlock() == Blocks.LAVA && blockState.getFluidState().isSource()) {
            LOGGER.info("IT'S LAVA");
            event.setCanceled(true);

            level.setBlock(result.getBlockPos(), Blocks.AIR.defaultBlockState(), 11);

            ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
            if (!player.getInventory().add(obsidian)) {
                player.drop(obsidian, false);
            }

            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
