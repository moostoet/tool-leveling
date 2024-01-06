package com.viridian.toolleveling;


import ca.weblite.objc.Client;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.viridian.toolleveling.capability.tool.ToolAbilities;
import com.viridian.toolleveling.capability.tool.ToolAbility;
import com.viridian.toolleveling.capability.tool.ToolExperience;
import com.viridian.toolleveling.entities.HighlightEntity;
import com.viridian.toolleveling.entities.HighlightEntityRenderer;
import com.viridian.toolleveling.entities.HighlightModel;
import com.viridian.toolleveling.entities.color.ColorMapping;
import com.viridian.toolleveling.networking.ToolLevelingNetwork;
import com.viridian.toolleveling.render.xp.ClientXPBarTooltipComponent;
import com.viridian.toolleveling.render.xp.XPBarTooltipComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import static com.viridian.toolleveling.attachment.AttachmentTypes.ATTACHMENT_TYPES;
import static com.viridian.toolleveling.attachment.AttachmentTypes.TOOL_EXP;

@Mod(ToolLeveling.MODID)
public class ToolLeveling {

    public static final String MODID = "toolleveling";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<HighlightEntity>> HIGHLIGHT_ENTITY = ENTITY_TYPES.register("highlight_entity",
            () -> EntityType.Builder.of(HighlightEntity::new, MobCategory.MISC)
                    .sized(1, 1)
                    .noSummon()
                    .build("highlight_entity"));

    public ToolLeveling(IEventBus modEventBus) {
        ToolLevelingNetwork.registerMessages();
        ATTACHMENT_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onRegisterTooltipComponent);
        modEventBus.addListener(this::onRegisterRenderers);
        modEventBus.addListener(this::onRegisterLayerDefinitions);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommandsEvent);
        NeoForge.EVENT_BUS.addListener(this::onRenderToolTipEvent);
//        NeoForge.EVENT_BUS.addListener(this::onTickEvent);
        NeoForge.EVENT_BUS.addListener(this::onBlockBreakEvent);
        NeoForge.EVENT_BUS.addListener(this::onPlayerInteractRightClickItem);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }


    public void onRegisterTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
        LOGGER.info("registering tooltip...");
        event.register(XPBarTooltipComponent.class, ClientXPBarTooltipComponent::new);
    }

    public void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(HIGHLIGHT_ENTITY.get(), HighlightEntityRenderer::new);
    }

    public void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(HighlightModel.LAYER_LOCATION, HighlightModel::createLayer);
    }

    @SubscribeEvent
    public void onRenderToolTipEvent(RenderTooltipEvent.GatherComponents e) {
        ItemStack item = e.getItemStack();

        if (isItemStackATool(item)) {
            ToolExperience toolExperience = item.getData(TOOL_EXP);
            CompoundTag toolExpData = toolExperience.serializeNBT();
            double experiencePercentage = toolExperience.getExperiencePercentage();

            XPBarTooltipComponent xpBarTooltipComponent = new XPBarTooltipComponent(experiencePercentage);
            renderTooltip(e, toolExpData, toolExperience);
            e.getTooltipElements().add(3, Either.right(xpBarTooltipComponent));
        }
    }

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("addability")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("ability", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (ToolAbility ability : ToolAbility.values()) {
                                        builder.suggest(ability.name());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String abilityName = StringArgumentType.getString(context, "ability");
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer player = source.getPlayerOrException();

                                    ItemStack heldItem = player.getMainHandItem();
                                    ToolExperience tool = heldItem.getData(TOOL_EXP);

                                    // Check if the player is holding an appropriate item
                                    if (heldItem.isEmpty()) {
                                        source.sendFailure(Component.translatable("command.addability.no_item"));
                                        return 0;
                                    }

                                    ToolAbilities toolAbilities = tool.getToolAbilities();
                                    try {
                                        ToolAbility abilityToAdd = ToolAbility.valueOf(abilityName.toUpperCase());
                                        toolAbilities.addAbility(abilityToAdd);
                                        source.sendSuccess(() -> Component.translatable("command.addability.success", abilityToAdd.getDisplayName()), true);
                                    } catch (IllegalArgumentException e) {
                                        source.sendFailure(Component.translatable("command.addability.invalid", abilityName));
                                    }
                                    return 1;
                                })
                        )
        );
    }

//    @SubscribeEvent
//    public void onTickEvent(TickEvent.ServerTickEvent e) {
//        LOGGER.info("server tick...");
//    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        ItemStack tool = event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND);

//        ToolLevelingNetwork.NETWORK.sendToServer(new TestMessage("BLOCK BROKEN"));
//        LOGGER.info(String.valueOf(event.getState().getBlock() instanceof CropBlock));

        if (tool.getItem() instanceof DiggerItem) {
            float hardness = event.getState().getDestroySpeed(event.getLevel(), event.getPos());
            boolean isCorrectTool = event.getPlayer().hasCorrectToolForDrops(event.getState());

            if (hardness > 0.2 && isCorrectTool) {
                ToolExperience toolExperience = tool.getData(TOOL_EXP);
                boolean leveledUp = toolExperience.addExperience(1);
                toolExperience.checkLeveledUp(leveledUp, event);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ToolExperience tool = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getData(TOOL_EXP);

        tool.getToolAbilities().handleRightClickItemAbilities(event);
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        Player player = e.getEntity();
        ItemStack heldItem = player.getMainHandItem();

        ToolExperience tool = e.getEntity().getItemInHand(InteractionHand.MAIN_HAND).getData(TOOL_EXP);

        if (heldItem.getItem() instanceof PickaxeItem) {
            tool.getToolAbilities().handleRightClickBlockAbilities(e);
        }
    }

    public static boolean isItemStackATool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DiggerItem;
    }

    public void renderTooltip(RenderTooltipEvent.GatherComponents event, CompoundTag toolExpData, ToolExperience toolExperience) {
        event.getTooltipElements().add(1, Either.left(Component.translatable("tooltip.toolleveling.level",
                toolExpData.getInt("Level"))));
        event.getTooltipElements().add(2, Either.left(Component.empty()));
        CompoundTag stats = toolExpData.getCompound("ToolStats");
        String formattedSpeed = "%.1f".formatted(stats.getFloat("MiningSpeed"));
        if (Screen.hasShiftDown()) {
            event.getTooltipElements().add(4, Either.left(Component.translatable("tooltip.toolleveling.experience", toolExpData.getInt("Experience"), toolExpData.getInt("NextLevelExperience"))));
            event.getTooltipElements().add(5, Either.left(Component.empty()));
            event.getTooltipElements().add(6, Either.left(Component.translatable("tooltip.toolleveling.stats", formattedSpeed, stats.getInt("FortuneLevel"))));
        } else {
            Component tooltipPart1 = Component.translatable("tooltip.toolleveling.showmore.1");
            Component tooltipPart2 = Component.translatable("tooltip.toolleveling.showmore.2");
            Component c = tooltipPart1.copy().append(tooltipPart2);
            event.getTooltipElements().add(4, Either.left(c));
            toolExperience.getToolAbilities().renderToolTips(event);
        }
    }
}
