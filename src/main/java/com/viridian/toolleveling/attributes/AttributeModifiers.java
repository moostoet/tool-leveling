package com.viridian.toolleveling.attributes;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class AttributeModifiers {
    public static final UUID DURABILITY_MODIFIER_ID = UUID.fromString("ee031231-a241-4c00-9b14-c5f3c8a50a43");

    public static final AttributeModifier DURABILITY_MODIFIER = new AttributeModifier(
            DURABILITY_MODIFIER_ID, "Durability bonus", 5.0, AttributeModifier.Operation.ADDITION
    );

    public static void applyModifiers(ItemStack stack, AttributeModifier... modifiers) {
        for (AttributeModifier modifier : modifiers) {

        }
    }
}
