package com.viridian.toolleveling.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class HighlightEntityRenderer extends EntityRenderer {
    HighlightModel model;

    public HighlightEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new HighlightModel(context.bakeLayer(HighlightModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(Entity p_114482_) {
        return null;
    }

    @Override
    public void render(Entity entity, float eYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        VertexConsumer outline = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.outline(new ResourceLocation("neoforge", "textures/white.png")));
        poseStack.pushPose();
        poseStack.translate(-.5, 0, -.5);
        model.renderToBuffer(poseStack, outline, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F,1F);
        poseStack.popPose();
        super.render(entity, eYaw, partialTick, poseStack, buffer, packedLight);
    }
}
