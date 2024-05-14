package com.github.standobyte.jojo.action.non_stand;

import com.github.standobyte.jojo.action.ActionConditionResult;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.ActionTarget.TargetType;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.HGStringEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanHornEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.PillarmanVeinEntity;
import com.github.standobyte.jojo.entity.damaging.projectile.ownerbound.SPStarFingerEntity;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData;
import com.github.standobyte.jojo.power.impl.nonstand.type.pillarman.PillarmanData.Mode;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.general.MathUtil;
import com.github.standobyte.jojo.util.mod.JojoModUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeHooks;

public class PillarmanErraticBlazeKing extends PillarmanAction {

    public PillarmanErraticBlazeKing(PillarmanAction.Builder builder) {
        super(builder);
        mode = Mode.HEAT;
    }

    @Override
    protected void perform(World world, LivingEntity user, INonStandPower power, ActionTarget target) {
        if (!world.isClientSide()) {
            int n = 7;
            for (int i = 0; i < n; i++) {
                Vector2f rotOffsets = MathUtil.xRotYRotOffsets((double) i / (double) n * Math.PI * 2, 5);
                addVeinProjectile(world, power, user, rotOffsets.y, rotOffsets.x);
            }
            addVeinProjectile(world, power, user, 0, 0);
        }
}

    public static void addVeinProjectile(World world, INonStandPower power, LivingEntity user, float yRotDelta, float xRotDelta) {
        PillarmanVeinEntity string = new PillarmanVeinEntity(world, user, yRotDelta, xRotDelta);
        string.setLifeSpan(20);
        world.addFreshEntity(string);
    }
    
}
