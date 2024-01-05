package com.viridian.toolleveling.render;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class XPBarTooltipComponent implements TooltipComponent {
    private final double experiencePercentage;

    public XPBarTooltipComponent(double experiencePercentage) {
        this.experiencePercentage = experiencePercentage;
    }

    public double getExperiencePercentage() {
        return experiencePercentage;
    }
}