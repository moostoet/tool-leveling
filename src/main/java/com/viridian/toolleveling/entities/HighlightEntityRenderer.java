package com.viridian.toolleveling.entities;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;

public class HighlightEntityRenderer extends EntityRenderer {
    HighlightModel model;

    private static final Logger LOGGER = LogUtils.getLogger();

    public HighlightEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HighlightModel(context.bakeLayer(HighlightModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(Entity p_114482_) {
        return new ResourceLocation("neoforge", "textures/white.png");
    }

    @Override
    public void render(Entity entity, float eYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int color = entity.getEntityData().get(HighlightEntity.getPersistentColor());
        int r = FastColor.ARGB32.red(color);
        int g = FastColor.ARGB32.green(color);
        int b = FastColor.ARGB32.blue(color);
//        int a = FastColor.ARGB32.alpha(color);
        int a = FastColor.ARGB32.alpha(color);

        Minecraft minecraft = Minecraft.getInstance();
        UUID playerUUID = minecraft.player.getUUID();

        if (!playerUUID.equals(entity.getEntityData().get(HighlightEntity.getPlayerUuid()).orElse(null))) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, (float) (a / 255));

        minecraft.renderBuffers().outlineBufferSource().setColor(r, g, b, a);

        VertexConsumer outline = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.outline(getTextureLocation(entity)));

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -0.5D);

        model.renderToBuffer(poseStack, outline, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, a);

        poseStack.popPose();
        super.render(entity, eYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        Optional<UUID> optionalEntityPlayerUUID = entity.getEntityData().get(HighlightEntity.getPlayerUuid());

        boolean shouldRender = optionalEntityPlayerUUID
                .map(uuid -> uuid.equals(Minecraft.getInstance().player.getUUID()))
                .orElse(false);
        return shouldRender && super.shouldRender(entity, p_114492_, p_114493_, p_114494_, p_114495_);
    }
}
