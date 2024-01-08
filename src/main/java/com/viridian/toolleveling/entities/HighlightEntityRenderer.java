package com.viridian.toolleveling.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.logging.LogUtils;
import com.viridian.toolleveling.render.custom.CustomRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.util.Optional;

public class HighlightEntityRenderer extends EntityRenderer {
    HighlightModel model;

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int FADE_IN_END = 20; // Time-to-live value where fade-in ends
    private static final int FADE_OUT_START = 10; // Time-to-live value where fade-out starts
    private static final int MAX_ALPHA = 255; // Maximum alpha value (fully opaque)
    public HighlightEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HighlightModel(context.bakeLayer(HighlightModel.LAYER_LOCATION));
    }

    private int fadeIn(int ttl, int maxTtl) {
        float progress = (float)(maxTtl - ttl) / (maxTtl - FADE_IN_END);
        return (int) Mth.lerp(progress, 0, MAX_ALPHA);
    }

    private int fadeOut(int ttl) {
        float progress = (float)ttl / FADE_OUT_START;
        return (int) Mth.lerp(progress, 0, MAX_ALPHA);
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
        int a = FastColor.ARGB32.alpha(color);

        int ttl = entity.getEntityData().get(HighlightEntity.getTtl());

        int alpha;
        if (ttl > FADE_IN_END) {
            alpha = fadeIn(ttl, 60);
        } else if (ttl < FADE_OUT_START) {
            alpha = fadeOut(ttl);
        } else {
            alpha = MAX_ALPHA;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (!entity.getEntityData().get(HighlightEntity.getPlayerUuid()).equals(Optional.of(minecraft.player.getUUID()))) return;

        minecraft.renderBuffers().outlineBufferSource().setColor(r, g, b, a);

        VertexConsumer outline = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(CustomRenderTypes.HIGHLIGHT_OUTLINE.apply(alpha));

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, -0.5D);

        model.renderToBuffer(poseStack, outline, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, a);

        poseStack.popPose();
        super.render(entity, eYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
