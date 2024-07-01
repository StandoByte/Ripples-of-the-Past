package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.JojoMod;

import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags.IOptionalNamedTag;

public class ModTags {
    public static final IOptionalNamedTag<EntityType<?>> HAMON_DAMAGE = EntityTypeTags.createOptional(new ResourceLocation(JojoMod.MOD_ID, "hamon_damage"));
    public static final IOptionalNamedTag<EntityType<?>> NO_HAMON_DAMAGE = EntityTypeTags.createOptional(new ResourceLocation(JojoMod.MOD_ID, "no_hamon_damage"));
    public static final IOptionalNamedTag<EntityType<?>> VAMPIRE_CAN_DRAIN = EntityTypeTags.createOptional(new ResourceLocation(JojoMod.MOD_ID, "vampire_can_drain"));
    public static final IOptionalNamedTag<EntityType<?>> VAMPIRE_CANNOT_DRAIN = EntityTypeTags.createOptional(new ResourceLocation(JojoMod.MOD_ID, "vampire_cannot_drain"));
    
    public static void initTags() {}

}
