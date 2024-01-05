package com.viridian.toolleveling.render.xp;

import com.viridian.toolleveling.ToolLeveling;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ClientXPBarTooltipComponent implements ClientTooltipComponent {
    private final XPBarTooltipComponent xpBarComponent;

    public ClientXPBarTooltipComponent(XPBarTooltipComponent xpBarComponent) {
        this.xpBarComponent = xpBarComponent;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public int getWidth(Font p_169952_) {
        return 150;
    }

    @Override
    public void renderText(Font p_169953_, int p_169954_, int p_169955_, Matrix4f p_253692_, MultiBufferSource.BufferSource p_169957_) {
        ClientTooltipComponent.super.renderText(p_169953_, p_169954_, p_169955_, p_253692_, p_169957_);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        ResourceLocation xpBarTexture = new ResourceLocation(ToolLeveling.MODID, "textures/xp.png");
        int barWidth = (int) (xpBarComponent.getExperiencePercentage() * 125);


        guiGraphics.blit(xpBarTexture, x, y, 0, 0, 125, 8, 125, 16);
        guiGraphics.blit(xpBarTexture, x, y, 0, 8, barWidth, 8, 125, 16);

        ClientTooltipComponent.super.renderImage(font, x, y, guiGraphics);
    }
}
