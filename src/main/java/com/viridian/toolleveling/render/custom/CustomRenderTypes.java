package com.viridian.toolleveling.render.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

// public net.minecraft.client.renderer.RenderType$OutlineProperty
// public net.minecraft.client.renderer.RenderType$CompositeState states
public final class CustomRenderTypes extends RenderType
{
    private static final RenderType.CompositeState OUTLINE_STATE = RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_OUTLINE_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation("neoforge", "textures/white.png"), false, false))
            .setCullState(NO_CULL)
            .setDepthTestState(NO_DEPTH_TEST)
            .setOutputState(OUTLINE_TARGET)
            .createCompositeState(RenderType.OutlineProperty.IS_OUTLINE);
    public static final Function<Integer, RenderType> HIGHLIGHT_OUTLINE = Util.memoize(alpha -> new RenderType(
            "highlight_outline",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            () ->
            {
                OUTLINE_STATE.states.forEach(RenderStateShard::setupRenderState);
                RenderSystem.setShaderColor(1F, 1F, 1F, alpha / 255F);
            },
            () ->
            {
                RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                OUTLINE_STATE.states.forEach(RenderStateShard::clearRenderState);
            }
    )
    {
        @Override
        public boolean isOutline()
        {
            return true;
        }

        @Override
        public String toString()
        {
            return "RenderType[" + this.name + ":" + OUTLINE_STATE + "]";
        }
    });



    private CustomRenderTypes()
    {
        //noinspection ConstantConditions
        super("", null, null, 0, false, false, null, null);
        throw new UnsupportedOperationException();
    }
}