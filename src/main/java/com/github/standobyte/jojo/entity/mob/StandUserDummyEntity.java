package com.github.standobyte.jojo.entity.mob;

import java.util.Optional;

import com.github.standobyte.jojo.action.Action;
import com.github.standobyte.jojo.action.ActionTarget;
import com.github.standobyte.jojo.action.stand.StandAction;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.init.power.JojoCustomRegistries;
import com.github.standobyte.jojo.item.StandDiscItem;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandPower;
import com.github.standobyte.jojo.util.mc.MCUtil;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class StandUserDummyEntity extends MobEntity implements IMobStandUser, IEntityAdditionalSpawnData {
    private IStandPower stand = new StandPower(this);
    private StandAction action;
    private boolean useAction = false;
    
    public StandUserDummyEntity(World world) {
        super(ModEntityTypes.STAND_USER_DUMMY.get(), world);
    }
    
    public StandUserDummyEntity(EntityType<? extends StandUserDummyEntity> type, World world) {
        super(type, world);
    }
    
    @Override
    public void onSyncedDataUpdated(DataParameter<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == CommonReflection.getEntityCustomNameParameter() && !level.isClientSide()) {
            updateStandAction();
        }
    }
    
    private void updateStandAction() {
        this.action = null;
        ITextComponent name = getCustomName();
        if (name != null) {
            String nameStr = name.getString();
            if (nameStr.contains(":")) {
                this.action = standActionFromId(new ResourceLocation(nameStr));
            }
            else if (stand.hasPower()) {
                ResourceLocation standId = stand.getType().getRegistryName();
                this.action = standActionFromId(new ResourceLocation(standId.getNamespace(), standId.getPath() + "_" + nameStr));
            }
        }
    }
    
    private static StandAction standActionFromId(ResourceLocation id) {
        if (JojoCustomRegistries.ACTIONS.getRegistry().containsKey(id)) {
            Action<?> action = JojoCustomRegistries.ACTIONS.getRegistry().getValue(id);
            if (action instanceof StandAction) {
                return (StandAction) action;
            }
        }
        return null;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide() && stand.hasPower() && action != null && useAction) {
            stand.clickAction(action, false, ActionTarget.EMPTY, null);
        }
        stand.tick();
    }
    
    @Override
    protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (item.getItem() == ModItems.STAND_DISC.get()) {
            if (!this.level.isClientSide) {
                Optional<StandInstance> previousDiscStand = stand.putOutStand();
                previousDiscStand.ifPresent(prevStand -> player.drop(StandDiscItem.withStand(
                        new ItemStack(ModItems.STAND_DISC.get()), prevStand), false));
                
                StandInstance discStand = StandDiscItem.getStandFromStack(item, false);
                if (StandDiscItem.giveStandFromDisc(stand, discStand, item)) {
                    stand.skipProgression();
                    if (!player.abilities.instabuild) {
                        item.shrink(1);
                    }
                    updateStandAction();
                }
                return ActionResultType.SUCCESS;
            } else {
                return ActionResultType.CONSUME;
            }
        }
        else {
            if (!level.isClientSide()) {
                if (player.isShiftKeyDown()) {
                    useAction = !useAction;
                    if (!useAction) {
                        stand.stopHeldAction(false);
                    }
                }
                else {
                    stand.toggleSummon();
                }
            }
            return ActionResultType.sidedSuccess(level.isClientSide());
        }
    }
    
    @Override
    public IStandPower getStandPower() {
        return stand;
    }
    
    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
    }
    
    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.put("Stand", stand.writeNBT());
    }
    
    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.contains("Stand", MCUtil.getNbtId(CompoundNBT.class))) {
            stand.readNBT(nbt.getCompound("Stand"));
            updateStandAction();
        }
    }
}
