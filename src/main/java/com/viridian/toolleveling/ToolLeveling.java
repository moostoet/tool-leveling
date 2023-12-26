package com.viridian.toolleveling;


import com.mojang.logging.LogUtils;
import com.viridian.toolleveling.capability.tool.ToolExperience;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.util.function.Supplier;

@Mod(ToolLeveling.MODID)
public class ToolLeveling {

    public static final String MODID = "toolleveling";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    private static final Supplier<AttachmentType<ToolExperience>> TOOL_EXP = ATTACHMENT_TYPES.register(
            "tool_experience", () -> AttachmentType.serializable(ToolExperience::new).build());

    public ToolLeveling(IEventBus modEventBus) {
        LOGGER.info("Hello, world");
        ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        LOGGER.info("TOOL_EXP INFO: " + TOOL_EXP);
        NeoForge.EVENT_BUS.addListener(this::onItemTooltipEvent);
        NeoForge.EVENT_BUS.addListener(this::onBlockBreakEvent);
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

            event.getToolTip().add(1, Component.translatable("tooltip.toolleveling.level",
                    toolExpData.getInt("Level")));
            event.getToolTip().add(2, Component.empty());
            event.getToolTip().add(3, Component.literal(toolExperience.buildExpBar()));
            event.getToolTip().add(4, Component.empty());
            if (Screen.hasShiftDown())
                event.getToolTip().add(5, Component.translatable("tooltip.toolleveling.experience", toolExpData.getInt("Experience"), toolExpData.getInt("NextLevelExperience")));
        }
    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);
        if (tool.getItem() instanceof DiggerItem) {
            float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
            boolean isCorrectTool = event.getPlayer().hasCorrectToolForDrops(event.getState());

            if (hardness > 0.2 && isCorrectTool) {
                LOGGER.info("PLAYER BROKE BLOCK WITH CORRECT TOOL:" + event);
                ToolExperience toolExperience = tool.getData(TOOL_EXP);
                if (toolExperience != null) {
                    boolean leveledUp = toolExperience.addExperience(1);
                    if (leveledUp) {
                        ((Level) event.getLevel()).playSound(null, event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.5F);
                        event.getPlayer().sendSystemMessage(Component.translatable("chat.tooltip.levelup", toolExperience.serializeNBT().getInt("Level")));
                    }
                }
            }
        }
    }

    public static boolean isItemStackATool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DiggerItem;
    }
}
