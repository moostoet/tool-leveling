package com.viridian.toolleveling.capability.tool;

import net.minecraft.network.chat.Component;

public enum ToolAbility {
    MAGMA_ABSORPTION("Magma Absorption"),
    VEIN_SEEKER("Vein Seeker"),
    SHOCKWAVE("Shockwave");
    private final String displayName;

    ToolAbility(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

    public Component getDisplayComponent() {
        return Component.literal(displayName);
    }

    }
