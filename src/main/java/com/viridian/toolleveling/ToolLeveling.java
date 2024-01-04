package com.viridian.toolleveling;


import com.mojang.logging.LogUtils;
import com.viridian.toolleveling.capability.tool.ToolAbility;
import com.viridian.toolleveling.capability.tool.ToolExperience;
import com.viridian.toolleveling.networking.TestMessage;
import com.viridian.toolleveling.networking.ToolLevelingNetwork;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

import static com.viridian.toolleveling.attachment.AttachmentTypes.ATTACHMENT_TYPES;
import static com.viridian.toolleveling.attachment.AttachmentTypes.TOOL_EXP;

@Mod(ToolLeveling.MODID)
public class ToolLeveling {

    public static final String MODID = "toolleveling";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ToolLeveling(IEventBus modEventBus) {
        ToolLevelingNetwork.registerMessages();
        LOGGER.info("Hello, world");
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("TOOL_EXP INFO: " + TOOL_EXP);
        NeoForge.EVENT_BUS.addListener(this::onItemTooltipEvent);
        NeoForge.EVENT_BUS.addListener(this::onBlockBreakEvent);
        NeoForge.EVENT_BUS.addListener(this::onPlayerInteractRightClickBlock);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onItemTooltipEvent(ItemTooltipEvent event) {
        if (isItemStackATool(event.getItemStack())) {
            ToolExperience toolExperience = event.getItemStack().getData(TOOL_EXP);
            CompoundTag toolExpData = toolExperience.serializeNBT();
            LOGGER.info("TOOLEXPDATA: " + toolExpData);

            renderTooltip(event, toolExpData, toolExperience);
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);

//        ToolLevelingNetwork.NETWORK.sendToServer(new TestMessage("BLOCK BROKEN"));
//        LOGGER.info(String.valueOf(event.getState().getBlock() instanceof CropBlock));

        if (tool.getItem() instanceof DiggerItem) {
            float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
            boolean isCorrectTool = event.getPlayer().hasCorrectToolForDrops(event.getState());

            if (hardness > 0.2 && isCorrectTool) {
                LOGGER.info("PLAYER BROKE BLOCK WITH CORRECT TOOL:" + event);
                ToolExperience toolExperience = tool.getData(TOOL_EXP);
                boolean leveledUp = toolExperience.addExperience(1);
                toolExperience.checkLeveledUp(leveledUp, event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ToolExperience tool = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getData(TOOL_EXP);
        tool.getToolAbilities().addAbility(ToolAbility.MAGMA_ABSORPTION);

        tool.getToolAbilities().handleRightClickAbilities(event);
    }

    public static boolean isItemStackATool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DiggerItem;
    }

    public void renderTooltip(ItemTooltipEvent event, CompoundTag toolExpData, ToolExperience toolExperience) {
        event.getToolTip().add(1, Component.translatable("tooltip.toolleveling.level",
                toolExpData.getInt("Level")));
        event.getToolTip().add(2, Component.empty());
        event.getToolTip().add(3, Component.literal(toolExperience.buildExpBar()));
        CompoundTag stats = toolExpData.getCompound("ToolStats");
        String formattedSpeed = "%.1f".formatted(stats.getFloat("MiningSpeed"));
        if (Screen.hasShiftDown()) {
            event.getToolTip().add(4, Component.translatable("tooltip.toolleveling.experience", toolExpData.getInt("Experience"), toolExpData.getInt("NextLevelExperience")));
            event.getToolTip().add(5, Component.empty());
            event.getToolTip().add(6, Component.translatable("tooltip.toolleveling.stats", formattedSpeed, stats.getInt("FortuneLevel")));
        } else {
            event.getToolTip().add(4, Component.empty());
            Component tooltipPart1 = Component.translatable("tooltip.toolleveling.showmore.1");
            Component tooltipPart2 = Component.translatable("tooltip.toolleveling.showmore.2");
            Component c = tooltipPart1.copy().append(tooltipPart2);
            event.getToolTip().add(5, c);
            toolExperience.getToolAbilities().renderToolTips(event);
        }
    }
}
