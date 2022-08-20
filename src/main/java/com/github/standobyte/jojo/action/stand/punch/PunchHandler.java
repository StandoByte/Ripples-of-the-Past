package com.github.standobyte.jojo.action.stand.punch;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.util.damage.StandEntityDamageSource;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class PunchHandler {
    private final EntityPunchFactory entityPunch;
    private final BlockPunchFactory blockPunch;
    private final MissedPunchFactory missedPunch;
    
    private PunchHandler(Builder builder) {
        this.entityPunch = builder.entityPunch;
        this.blockPunch = builder.blockPunch;
        this.missedPunch = builder.missedPunch;
    }
    
    public StandEntityPunch punchEntity(StandEntity stand, Entity target, StandEntityDamageSource dmgSource) {
        return entityPunch.create(stand, target, dmgSource);
    }
    
    public StandBlockPunch punchBlock(StandEntity stand, BlockPos pos, BlockState state) {
        return blockPunch.create(stand, pos, state);
    }
    
    public StandMissedPunch punchMissed(StandEntity stand) {
        return missedPunch.create(stand);
    }
    
    
    
    public static class Builder {
        private EntityPunchFactory entityPunch = StandEntityPunch::new;
        private BlockPunchFactory blockPunch = StandBlockPunch::new;
        private MissedPunchFactory missedPunch = StandMissedPunch::new;
        
        public Builder setEntityPunch(EntityPunchFactory entityPunch) {
            this.entityPunch = entityPunch;
            return this;
        }

        public Builder modifyEntityPunch(UnaryOperator<StandEntityPunch> after) {
            return setEntityPunch(entityPunch.andThen(after));
        }
        
        public Builder setBlockPunch(BlockPunchFactory blockPunch) {
            this.blockPunch = blockPunch;
            return this;
        }

        public Builder modifyBlockPunch(UnaryOperator<StandBlockPunch> after) {
            return setBlockPunch(blockPunch.andThen(after));
        }
        
        public Builder setMissedPunch(MissedPunchFactory emptyPunch) {
            this.missedPunch = emptyPunch;
            return this;
        }

        public Builder modifyMissedPunch(UnaryOperator<StandMissedPunch> after) {
            return setMissedPunch(missedPunch.andThen(after));
        }
        
        public Builder setPunchSound(Supplier<SoundEvent> sound) {
            modifyEntityPunch(punch -> punch.setPunchSound(sound));
            modifyBlockPunch(punch -> punch.setPunchSound(sound));
            return this;
        }
        
        public PunchHandler build() {
            return new PunchHandler(this);
        }
    }
    
    @FunctionalInterface
    public static interface EntityPunchFactory {
        StandEntityPunch create(StandEntity stand, Entity target, StandEntityDamageSource dmgSource);
        
        default EntityPunchFactory andThen(UnaryOperator<StandEntityPunch> after) {
            Objects.requireNonNull(after);
            return (StandEntity stand, Entity target, StandEntityDamageSource dmgSource)
                    -> after.apply(create(stand, target, dmgSource));
        }
    }
    
    @FunctionalInterface
    public static interface BlockPunchFactory {
        StandBlockPunch create(StandEntity stand, BlockPos blockPos, BlockState blockState);
        
        default BlockPunchFactory andThen(UnaryOperator<StandBlockPunch> after) {
            Objects.requireNonNull(after);
            return (StandEntity stand, BlockPos blockPos, BlockState blockState)
                    -> after.apply(create(stand, blockPos, blockState));
        }
    }
    
    @FunctionalInterface
    public static interface MissedPunchFactory {
        StandMissedPunch create(StandEntity stand);
        
        default MissedPunchFactory andThen(UnaryOperator<StandMissedPunch> after) {
            Objects.requireNonNull(after);
            return (StandEntity stand) -> after.apply(create(stand));
        }}
}
