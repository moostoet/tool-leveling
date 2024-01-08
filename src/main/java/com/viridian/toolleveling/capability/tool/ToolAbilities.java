package com.viridian.toolleveling.capability.tool;

import com.mojang.datafixers.util.Either;
import com.viridian.toolleveling.entities.HighlightEntity;
import com.viridian.toolleveling.entities.color.ColorMapping;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Tool;
import java.util.*;

import static com.viridian.toolleveling.ToolLeveling.HIGHLIGHT_ENTITY;

public class ToolAbilities implements INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolAbilities.class);
    private final Set<ToolAbility> abilities;

    private final Object2LongMap<ToolAbility> lastAbilityUseTime;
    private final Map<ToolAbility, Integer> abilityCooldowns;

    public ToolAbilities() {
        this.abilities = EnumSet.noneOf(ToolAbility.class);
        this.lastAbilityUseTime = new Object2LongOpenHashMap<>();
        this.abilityCooldowns = new HashMap<>();

        abilityCooldowns.put(ToolAbility.MAGMA_ABSORPTION, 100);
        abilityCooldowns.put(ToolAbility.VEIN_SEEKER, 200);
    }

    public boolean isAbilityReady(Level level, ToolAbility ability) {
        return level.getGameTime() - lastAbilityUseTime.getLong(ability) >= abilityCooldowns.getOrDefault(ability, 0);
    }

    public void useAbility(Level level, ToolAbility ability) {
        lastAbilityUseTime.put(ability, level.getGameTime());
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

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag abilitiesList = new ListTag();
        for (ToolAbility ability : abilities) {
            abilitiesList.add(StringTag.valueOf(ability.name()));
        }
        tag.put("Abilities", abilitiesList);

        CompoundTag cooldownsTag = new CompoundTag();
        for (ToolAbility ability : lastAbilityUseTime.keySet()) {
            cooldownsTag.putLong(ability.name(), lastAbilityUseTime.getLong(ability));
        }
        tag.put("Cooldowns", cooldownsTag);

        return tag;
    }

    @Override
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

        if (tag.contains("Cooldowns", CompoundTag.TAG_COMPOUND)) {
            CompoundTag cooldownsTag = tag.getCompound("Cooldowns");
            for (ToolAbility ability : abilityCooldowns.keySet()) {
                if (cooldownsTag.contains(ability.name())) {
                    lastAbilityUseTime.put(ability, cooldownsTag.getLong(ability.name()));
                }
            }
        }
    }

    public void renderToolTips(RenderTooltipEvent.GatherComponents event) {
        if (!abilities.isEmpty()) {
            int index = 5;
            event.getTooltipElements().add(index, Either.left(Component.empty()));
            for (ToolAbility ability : abilities) {
                index++;
                event.getTooltipElements().add(index, Either.left(ability.getDisplayComponent()));
            }
        }
    }

    public void handleRightClickItemAbilities(PlayerInteractEvent.RightClickItem event) {
        if (this.hasAbility(ToolAbility.MAGMA_ABSORPTION)) handleMagmaAbsorptionAbility(event);
    }

    public void handleRightClickBlockAbilities(PlayerInteractEvent.RightClickBlock event) {
        if (this.hasAbility(ToolAbility.VEIN_SEEKER)) handleVeinSeekerAbility(event);
    }

    private void handleVeinSeekerAbility(PlayerInteractEvent.RightClickBlock event) {
        int range = 16;
        Level level = event.getLevel();
        BlockPos center = event.getPos();

        if (!isAbilityReady(level, ToolAbility.VEIN_SEEKER)) return;

        useAbility(level, ToolAbility.VEIN_SEEKER);


        event.getEntity().swing(InteractionHand.MAIN_HAND);
        level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, 0.9F);
        spawnVeinParticles(level, center);

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range))) {
            BlockState bState = level.getBlockState(pos);
            if (bState.is(Tags.Blocks.ORES)) {
                if (!level.isClientSide) {
                    EntityType<HighlightEntity> highlightEntityType = HIGHLIGHT_ENTITY.get();

                    HighlightEntity highlightEntity = highlightEntityType.create(level);

                    Integer color = ColorMapping.getColorForBlock(bState.getBlock());

                    if (color != null) {
                        highlightEntity.getEntityData().set(HighlightEntity.getPersistentColor(), color);
                    }

                    highlightEntity.getEntityData().set(HighlightEntity.getTtl(), 60);
                    highlightEntity.getEntityData().set(HighlightEntity.getPlayerUuid(), Optional.of(event.getEntity().getUUID()));

                    highlightEntity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    level.addFreshEntity(highlightEntity);
                }
            }
        }
    }

    public static void spawnVeinParticles(Level level, BlockPos pos) {
        for (int i = 0; i < 20; ++i) {
            double d0 = pos.getX() + level.random.nextDouble();
            double d1 = pos.getY() + level.random.nextDouble();
            double d2 = pos.getZ() + level.random.nextDouble();
            level.addParticle(ParticleTypes.GLOW, d0, d1, d2, 0.1, 0.1, 0.1);
        }
    }

    private void handleMagmaAbsorptionAbility(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();

        if (!isAbilityReady(level, ToolAbility.MAGMA_ABSORPTION)) return;

        useAbility(level, ToolAbility.MAGMA_ABSORPTION);

        BlockHitResult result = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        BlockState blockState = level.getBlockState(result.getBlockPos());

        if (blockState.getBlock() == Blocks.LAVA && blockState.getFluidState().isSource()) {
            LOGGER.info("IT'S LAVA");
            event.setCanceled(true);

            level.setBlock(result.getBlockPos(), Blocks.AIR.defaultBlockState(), 11);

            spawnMagmaParticles(level, result.getBlockPos());

            ItemStack obsidian = new ItemStack(Blocks.OBSIDIAN);
            if (!player.getInventory().add(obsidian)) {
                player.drop(obsidian, false);
            }

            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private void spawnMagmaParticles(Level level, BlockPos pos) {
        for (int i = 0; i < 20; ++i) {
            double d0 = pos.getX() + level.random.nextDouble();
            double d1 = pos.getY() + 1;
            double d2 = pos.getZ() + level.random.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0, 0.1, 0);
            level.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0, 0.1, 0);
        }
    }
}
