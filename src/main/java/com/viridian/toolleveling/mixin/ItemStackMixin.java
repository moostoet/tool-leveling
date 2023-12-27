package com.viridian.toolleveling.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.viridian.toolleveling.capability.tool.ToolExperience;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.viridian.toolleveling.attachment.AttachmentTypes.TOOL_EXP;


@Mixin(ItemStack.class)
public class ItemStackMixin {
    //We are targeting the invocation of Item#getDestroySpeed in ItemStack#getDestroySpeed.
    //The value of method must be a valid method name. If overloads exist, you must also specify the parameters.
    //The target is expressed as the bytecode signature of the operation we want to wrap. Google bytecode signatures, they're pretty easy to grasp.
    @WrapOperation(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDestroySpeed(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float /*return type must match the original return type*/ getToolLevelModifiedDestroySpeedToolLeveling /*name can be chosen at will, just don't pick one that's already present in your target class*/ (
            Item instance, //the instance this method is called on
            ItemStack stack,
            BlockState blockstate, //the parameters of the original call, we only have one here; if there's multiple, just list all of them as usual
            Operation<Float> original //the original operation. Should be called on at some point to allow other mods' logic from running as well.
    ) {
        float currentDestroySpeed = original.call(instance, stack, blockstate);

        ToolExperience toolExperience = stack.getData(TOOL_EXP);

        float miningSpeedModifier = toolExperience.getToolStats().getMiningSpeed();
        if (currentDestroySpeed > 1.0f) currentDestroySpeed += miningSpeedModifier;

        return currentDestroySpeed;
    }
}
