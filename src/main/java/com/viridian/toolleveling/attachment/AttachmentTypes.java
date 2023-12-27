package com.viridian.toolleveling.attachment;

import com.viridian.toolleveling.capability.tool.ToolExperience;
import com.viridian.toolleveling.capability.tool.ToolStats;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class AttachmentTypes {
    public static final String MODID = "toolleveling";
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID);
    public static final Supplier<AttachmentType<ToolExperience>> TOOL_EXP = ATTACHMENT_TYPES.register(
            "tool_experience", () -> AttachmentType.serializable(ToolExperience::new).build());
}
