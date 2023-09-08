package com.github.standobyte.jojo.util.mc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromclient.ClRequestMobAggroPacket;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public enum MobAggroCategory {
    /* TODO
     * conditional aggro -> neutral
     *   spider
     *   wolf
     *   bee
     *   enderman
     *   iron golem
     *   polar bear
     * these aren't passive
     *   guardian/elder guardian
     *   phantom
     *   ender dragon
     *   piglin (neutral)
     *   piglin brute
     *   zoglin
     * wither isn't neutral
     */
    PASSIVE(new TranslationTextComponent("lifeform.mob_category.passive").withStyle(TextFormatting.GREEN)),
    NEUTRAL(new TranslationTextComponent("lifeform.mob_category.neutral").withStyle(TextFormatting.YELLOW)),
    HOSTILE(new TranslationTextComponent("lifeform.mob_category.hostile").withStyle(TextFormatting.RED));
    
    private MobAggroCategory(ITextComponent name) {
        this.name = name;
    }
    private final ITextComponent name;
    
    private static final Map<EntityType<?>, MobAggroCategory> CLASSIFICATION = new HashMap<>();
    
    public static void requestCategoryOnClient(Collection<EntityType<?>> forEntityTypes) {
        forEntityTypes = forEntityTypes.stream().filter(type -> !CLASSIFICATION.containsKey(type)).collect(Collectors.toList());
        if (!forEntityTypes.isEmpty()) {
            PacketManager.sendToServer(new ClRequestMobAggroPacket(forEntityTypes));
        }
    }
    
    public static void setCategoryManually(EntityType<?> mobType, MobAggroCategory category) {
        CLASSIFICATION.put(mobType, category);
    }
    
    @Nullable
    public static MobAggroCategory getCategoryOnClient(EntityType<?> mobType) {
        return CLASSIFICATION.get(mobType);
    }
    
    @Nullable
    public static MobAggroCategory getCategoryOnServer(EntityType<?> mobType) {
        return CLASSIFICATION.computeIfAbsent(mobType, type -> {
            Entity entity = EntityTypeToInstance.getEntityInstance(type);
            if (entity instanceof MobEntity) {
                GoalSelector targetSelector = ((MobEntity) entity).targetSelector;
                Set<PrioritizedGoal> targets = CommonReflection.getGoalsSet(targetSelector);
                
                MobAggroCategory category = MobAggroCategory.PASSIVE;
                for (PrioritizedGoal target : targets) {
                    Goal targetType = target.getGoal();
                    if (targetType instanceof NearestAttackableTargetGoal) {
                        Class<?> targetClass = CommonReflection.getTargetClass((NearestAttackableTargetGoal<?>) targetType);
                        if (targetClass == PlayerEntity.class) {
                            return MobAggroCategory.HOSTILE;
                        }
                    }
                    else if (targetType instanceof HurtByTargetGoal) {
                        Class<?>[] ignoredClasses = CommonReflection.getToIgnoreDamage((HurtByTargetGoal) targetType);
                        if (!ArrayUtils.contains(ignoredClasses, PlayerEntity.class)) {
                            category = MobAggroCategory.NEUTRAL;
                        }
                    }
                }
                
                return category;
            }
            return null;
        });
    }
    
    public ITextComponent getName() {
        return name;
    }
}
