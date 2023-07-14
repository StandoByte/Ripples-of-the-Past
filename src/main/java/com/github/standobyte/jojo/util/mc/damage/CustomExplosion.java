package com.github.standobyte.jojo.util.mc.damage;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.CustomExplosionPacket;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ForgeEventFactory;

public abstract class CustomExplosion extends Explosion {
    
    private CustomExplosion(World pLevel, @Nullable Entity pSource, 
            @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
            double pToBlowX, double pToBlowY, double pToBlowZ, 
            float pRadius, boolean pFire, Explosion.Mode pBlockInteraction) {
        super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
    }
    
    
    
    
    
    public static Explosion explode(World pLevel, @Nullable Entity pEntity, 
            double pX, double pY, double pZ, float pExplosionRadius, Explosion.Mode pMode, CustomExplosionType explosionType) {
        return explode(pLevel, pEntity, (DamageSource)null, (ExplosionContext)null, pX, pY, pZ, pExplosionRadius, false, pMode, explosionType);
    }

    public static Explosion explode(World pLevel, @Nullable Entity pEntity, 
            double pX, double pY, double pZ, float pExplosionRadius, boolean pCausesFire, Explosion.Mode pMode, CustomExplosionType explosionType) {
        return explode(pLevel, pEntity, (DamageSource)null, (ExplosionContext)null, pX, pY, pZ, pExplosionRadius, pCausesFire, pMode, explosionType);
    }

    public static Explosion explode(World pLevel, @Nullable Entity pExploder, 
            @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pContext, 
            double pX, double pY, double pZ, float pSize, boolean pCausesFire, Explosion.Mode pMode, CustomExplosionType explosionType) {
        Explosion explosion = explosionType.createExplosion(pLevel, pExploder, 
                pDamageSource, pContext, 
                pX, pY, pZ, 
                pSize, pCausesFire, pMode);
        if (ForgeEventFactory.onExplosionStart(pLevel, explosion)) return explosion;
        explosion.explode();
        explosion.finalizeExplosion(true);
        
        if (!pLevel.isClientSide()) {
            if (pMode == Explosion.Mode.NONE) {
                explosion.clearToBlow();
            }

            for(ServerPlayerEntity player : ((ServerWorld) pLevel).players()) {
                if (player.distanceToSqr(pX, pY, pZ) < 4096.0D) {
                    PacketManager.sendToClient(new CustomExplosionPacket(pX, pY, pZ, 
                            pSize, explosion.getToBlow(), explosion.getHitPlayers().get(player), explosionType), player);
                }
            }
        }
        
        return explosion;
    }
    
    
    public static enum CustomExplosionType {
        PLACEHOLDER {
            @Override
            public Explosion createExplosion(World pLevel, Entity pSource, DamageSource pDamageSource,
                    ExplosionContext pDamageCalculator, double pToBlowX, double pToBlowY, double pToBlowZ,
                    float pRadius, boolean pFire, Mode pBlockInteraction) {
                return null;
            }
        };
        
        @Nonnull public abstract Explosion createExplosion(World pLevel, @Nullable Entity pSource, 
                @Nullable DamageSource pDamageSource, @Nullable ExplosionContext pDamageCalculator, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, boolean pFire, Explosion.Mode pBlockInteraction);
        
        @Nonnull public Explosion createExplosionOnClient(World pLevel, @Nullable Entity pSource, 
                double pToBlowX, double pToBlowY, double pToBlowZ, 
                float pRadius, List<BlockPos> pPositions) {
            Explosion explosion = createExplosion(pLevel, pSource, 
                    null, null, 
                    pToBlowX, pToBlowY, pToBlowZ, 
                    pRadius, false, Explosion.Mode.DESTROY);
            explosion.getToBlow().addAll(pPositions);
            return explosion;
        }
    }
}
