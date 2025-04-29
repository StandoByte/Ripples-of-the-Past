package com.github.standobyte.jojo.client.entity;

import java.util.Iterator;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ControllerConsciousness;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.CommandBlockScreen;
import net.minecraft.client.gui.screen.EditBookScreen;
import net.minecraft.client.gui.screen.EditMinecartCommandBlockScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.EditStructureScreen;
import net.minecraft.client.gui.screen.JigsawScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.JigsawTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;

public class ClientConsciousnessEntity extends AbstractClientPlayerEntity {

    public ClientConsciousnessEntity(ControllerConsciousness clController) {
        super(clController.mc.level, clController.mc.player.getGameProfile());
        this.minecraft = clController.mc;
        copyPosition(minecraft.player);
        this.setUUID(MathHelper.createInsecureUUID(this.random));
        this.input = minecraft.player.input;
        
        hurtTimerStart = minecraft.player.hurtTime;
        
        AttributeModifierManager attributes = getAttributes();
        AttributeModifierManager playerAttributes = minecraft.player.getAttributes();
        attributes.getInstance(Attributes.MOVEMENT_SPEED).setBaseValue(
                playerAttributes.getInstance(Attributes.MOVEMENT_SPEED).getValue() * 2);
        attributes.getInstance(Attributes.ATTACK_SPEED).setBaseValue(
                playerAttributes.getInstance(Attributes.ATTACK_SPEED).getValue() * 2);
        attributes.getInstance(ForgeMod.SWIM_SPEED.get()).setBaseValue(
                playerAttributes.getInstance(ForgeMod.SWIM_SPEED.get()).getValue() * 2);
    }

    
    
    private NetworkPlayerInfo playerInfo;
    @Override
    protected NetworkPlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        }
        return this.playerInfo;
    }
    
    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        return minecraft.player.isModelPartShown(part);
    }
    
    
    public boolean cancelPlayerRender = true;
    public void checkPlayerFrustum(MatrixStack matrixStack, Matrix4f projection, Vector3d cameraPos) {
        if (tickCount > 5 && cancelPlayerRender) {
            AxisAlignedBB playerAabb = minecraft.player.getBoundingBoxForCulling().inflate(0.5);
            if (playerAabb.hasNaN() || playerAabb.getSize() == 0) {
                playerAabb = new AxisAlignedBB(
                        minecraft.player.getX() - 2, 
                        minecraft.player.getY() - 2, 
                        minecraft.player.getZ() - 2, 
                        minecraft.player.getX() + 2, 
                        minecraft.player.getY() + 2, 
                        minecraft.player.getZ() + 2);
            }
            
            ClippingHelper camera = new ClippingHelper(matrixStack.last().pose(), projection);
            camera.prepare(cameraPos.x, cameraPos.y, cameraPos.z);
            if (camera != null && !camera.isVisible(playerAabb)) {
                cancelPlayerRender = false;
            }
        }
    }

    private int hurtTimerStart;
    private void tickPlayerRenderCancel() {
        if (minecraft.player.isOnFire() || tickCount > 2 && minecraft.player.hurtTime >= hurtTimerStart) {
            cancelPlayerRender = false;
        }
    }



    private boolean crouching;
    private boolean flashOnSetHealth;
    public MovementInput input;
    protected final Minecraft minecraft;
    protected int sprintTriggerTime;
    public int sprintTime;
    public float yBob;
    public float xBob;
    public float yBobO;
    public float xBobO;
    private int jumpRidingTicks;
    private float jumpRidingScale;
    public float portalTime;
    public float oPortalTime;
    private boolean startedUsingItem;
    private Hand usingItemHand;
    private boolean handsBusy;
    private boolean autoJumpEnabled = true;
    private int autoJumpTime;
    private boolean wasFallFlying;
    private int waterVisionTime;
    private boolean showDeathScreen = true;


    @Override
    public void push(Entity pEntity) {
    }

    @Override
    public void push(double pX, double pY, double pZ) {
    }

    @Override
    protected void pushEntities() {
    }



    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        ForgeHooks.onPlayerAttack(this, pSource, pAmount);
        return false;
    }

    /**
     * Heal living entity (param: amount of half-hearts)
     */
    @Override
    public void heal(float pHealAmount) {
    }

    @Override
    public boolean startRiding(Entity pEntity, boolean pForce) {
        return false;
    }

//    @Override
//    public void removeVehicle() {
//        super.removeVehicle();
//    }

    /**
     * Returns the current X rotation of the entity.
     */
    @Override
    public float getViewXRot(float pPartialTicks) {
        return this.xRot;
    }

    /**
     * Returns the current Y rotation of the entity.
     */
    @Override
    public float getViewYRot(float pPartialTick) {
        return this.isPassenger() ? super.getViewYRot(pPartialTick) : this.yRot;
    }

    @Override
    public void tick() {
        doTick();
    }
    
    public void doTick() {
        if (this.level.hasChunkAt(new BlockPos(this.getX(), 0.0D, this.getZ()))) {
            super.tick();
            this.autoJumpEnabled = this.minecraft.options.autoJump;
        }
        tickPlayerRenderCancel();
    }
    
//    @Override
//    public boolean drop(boolean pHasControlDown) {
//        CPlayerDiggingPacket.Action cplayerdiggingpacket$action = pHasControlDown ? CPlayerDiggingPacket.Action.DROP_ALL_ITEMS : CPlayerDiggingPacket.Action.DROP_ITEM;
//        this.connection.send(new CPlayerDiggingPacket(cplayerdiggingpacket$action, BlockPos.ZERO, Direction.DOWN));
//        return this.inventory.removeItem(this.inventory.selected, pHasControlDown && !this.inventory.getSelected().isEmpty() ? this.inventory.getSelected().getCount() : 1) != ItemStack.EMPTY;
//    }

    /**
     * Deals damage to the entity. This will take the armor of the entity into consideration before damaging the health
     * bar.
     */
    @Override
    protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (!this.isInvulnerableTo(pDamageSource)) {
            this.setHealth(this.getHealth() - pDamageAmount);
        }
    }

//    /**
//     * set current crafting inventory back to the 2x2 square
//     */
//    @Override
//    public void closeContainer() {
//        this.connection.send(new CCloseWindowPacket(this.containerMenu.containerId));
//        this.clientSideCloseContainer();
//    }
//
//    public void clientSideCloseContainer() {
//        this.inventory.setCarried(ItemStack.EMPTY);
//        super.closeContainer();
//        this.minecraft.setScreen((Screen)null);
//    }

    /**
     * Updates health locally.
     */
    public void hurtTo(float pHealth) {
        if (this.flashOnSetHealth) {
            float f = this.getHealth() - pHealth;
            if (f <= 0.0F) {
                this.setHealth(pHealth);
                if (f < 0.0F) {
                    this.invulnerableTime = 10;
                }
            } else {
                this.lastHurt = f;
                this.setHealth(this.getHealth());
                this.invulnerableTime = 20;
                this.actuallyHurt(DamageSource.GENERIC, f);
                this.hurtDuration = 10;
                this.hurtTime = this.hurtDuration;
            }
        } else {
            this.setHealth(pHealth);
            this.flashOnSetHealth = true;
        }

    }

    @Override
    public boolean isControlledByLocalInstance() {
        return true;
    }

    @Override
    public boolean isSuppressingSlidingDownLadder() {
        return !this.abilities.flying && super.isSuppressingSlidingDownLadder();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return !this.abilities.flying && super.canSpawnSprintParticle();
    }

    @Override
    public boolean canSpawnSoulSpeedParticle() {
        return !this.abilities.flying && super.canSpawnSoulSpeedParticle();
    }

    @Override
    public void displayClientMessage(ITextComponent pChatComponent, boolean pActionBar) {
        if (pActionBar) {
            this.minecraft.gui.setOverlayMessage(pChatComponent, false);
        } else {
            this.minecraft.gui.getChat().addMessage(pChatComponent);
        }

    }

    private void moveTowardsClosestSpace(double pX, double pZ) {
        BlockPos blockpos = new BlockPos(pX, this.getY(), pZ);
        if (this.suffocatesAt(blockpos)) {
            double d0 = pX - (double)blockpos.getX();
            double d1 = pZ - (double)blockpos.getZ();
            Direction direction = null;
            double d2 = Double.MAX_VALUE;
            Direction[] adirection = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};

            for(Direction direction1 : adirection) {
                double d3 = direction1.getAxis().choose(d0, 0.0D, d1);
                double d4 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d3 : d3;
                if (d4 < d2 && !this.suffocatesAt(blockpos.relative(direction1))) {
                    d2 = d4;
                    direction = direction1;
                }
            }

            if (direction != null) {
                Vector3d vector3d = this.getDeltaMovement();
                if (direction.getAxis() == Direction.Axis.X) {
                    this.setDeltaMovement(0.1D * (double)direction.getStepX(), vector3d.y, vector3d.z);
                } else {
                    this.setDeltaMovement(vector3d.x, vector3d.y, 0.1D * (double)direction.getStepZ());
                }
            }

        }
    }

    private boolean suffocatesAt(BlockPos pPos) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox();
        AxisAlignedBB axisalignedbb1 = (new AxisAlignedBB((double)pPos.getX(), axisalignedbb.minY, (double)pPos.getZ(), (double)pPos.getX() + 1.0D, axisalignedbb.maxY, (double)pPos.getZ() + 1.0D)).deflate(1.0E-7D);
        return !this.level.noBlockCollision(this, axisalignedbb1, (p_243494_1_, p_243494_2_) -> {
            return p_243494_1_.isSuffocating(this.level, p_243494_2_);
        });
    }

    /**
     * Set sprinting switch for Entity.
     */
    @Override
    public void setSprinting(boolean pSprinting) {
        super.setSprinting(pSprinting);
        this.sprintTime = 0;
    }

    /**
     * Sets the current XP, total XP, and level number.
     */
    public void setExperienceValues(float pCurrentXP, int pMaxXP, int pLevel) {
        this.experienceProgress = pCurrentXP;
        this.totalExperience = pMaxXP;
        this.experienceLevel = pLevel;
    }

    /**
     * Send a chat message to the CommandSender
     */
    @Override
    public void sendMessage(ITextComponent pComponent, UUID pSenderUUID) {
        this.minecraft.gui.getChat().addMessage(pComponent);
    }

    public void setShowDeathScreen(boolean pShow) {
        this.showDeathScreen = pShow;
    }

    public boolean shouldShowDeathScreen() {
        return this.showDeathScreen;
    }

    @Override
    public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
        PlaySoundAtEntityEvent event = ForgeEventFactory.onPlaySoundAtEntity(this, pSound, this.getSoundSource(), pVolume, pPitch);
        if (event.isCanceled() || event.getSound() == null) return;
        pSound = event.getSound();
        pVolume = event.getVolume();
        pPitch = event.getPitch();
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), pSound, this.getSoundSource(), pVolume, pPitch, false);
    }

    @Override
    public void playNotifySound(SoundEvent pSound, SoundCategory pSource, float pVolume, float pPitch) {
        this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), pSound, pSource, pVolume, pPitch, false);
    }

    /**
     * Returns whether the entity is in a server world
     */
    @Override
    public boolean isEffectiveAi() {
        return true;
    }

    @Override
    public void startUsingItem(Hand pHand) {
        ItemStack itemstack = this.getItemInHand(pHand);
        if (!itemstack.isEmpty() && !this.isUsingItem()) {
            super.startUsingItem(pHand);
            this.startedUsingItem = true;
            this.usingItemHand = pHand;
        }
    }

    @Override
    public boolean isUsingItem() {
        return this.startedUsingItem;
    }

    @Override
    public void stopUsingItem() {
        super.stopUsingItem();
        this.startedUsingItem = false;
    }

    @Override
    public Hand getUsedItemHand() {
        return this.usingItemHand;
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (DATA_LIVING_ENTITY_FLAGS.equals(pKey)) {
            boolean flag = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
            Hand hand = (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
            if (flag && !this.startedUsingItem) {
                this.startUsingItem(hand);
            } else if (!flag && this.startedUsingItem) {
                this.stopUsingItem();
            }
        }

//        if (DATA_SHARED_FLAGS_ID.equals(pKey) && this.isFallFlying() && !this.wasFallFlying) {
//            this.minecraft.getSoundManager().play(new ElytraSound(this));
//        }

    }

    public boolean isRidingJumpable() {
        Entity entity = this.getVehicle();
        return this.isPassenger() && entity instanceof IJumpingMount && ((IJumpingMount)entity).canJump();
    }

    public float getJumpRidingScale() {
        return this.jumpRidingScale;
    }

    @Override
    public void openTextEdit(SignTileEntity pSignTile) {
        this.minecraft.setScreen(new EditSignScreen(pSignTile));
    }

    @Override
    public void openMinecartCommandBlock(CommandBlockLogic pCommandBlock) {
        this.minecraft.setScreen(new EditMinecartCommandBlockScreen(pCommandBlock));
    }

    @Override
    public void openCommandBlock(CommandBlockTileEntity pCommandBlock) {
        this.minecraft.setScreen(new CommandBlockScreen(pCommandBlock));
    }

    @Override
    public void openStructureBlock(StructureBlockTileEntity pStructure) {
        this.minecraft.setScreen(new EditStructureScreen(pStructure));
    }

    @Override
    public void openJigsawBlock(JigsawTileEntity pJigsawBlockEntity) {
        this.minecraft.setScreen(new JigsawScreen(pJigsawBlockEntity));
    }

    @Override
    public void openItemGui(ItemStack pStack, Hand pHand) {
        Item item = pStack.getItem();
        if (item == Items.WRITABLE_BOOK) {
            this.minecraft.setScreen(new EditBookScreen(this, pStack, pHand));
        }

    }

    /**
     * Called when the entity is dealt a critical hit.
     */
    @Override
    public void crit(Entity pEntityHit) {
        this.minecraft.particleEngine.createTrackingEmitter(pEntityHit, ParticleTypes.CRIT);
    }

    @Override
    public void magicCrit(Entity pEntityHit) {
        this.minecraft.particleEngine.createTrackingEmitter(pEntityHit, ParticleTypes.ENCHANTED_HIT);
    }

    @Override
    public boolean isShiftKeyDown() {
        return this.input != null && this.input.shiftKeyDown;
    }

    @Override
    public boolean isCrouching() {
        return this.crouching;
    }

    public boolean isMovingSlowly() {
        return this.isCrouching() || this.isVisuallyCrawling();
    }

    @Override
    public void serverAiStep() {
        super.serverAiStep();
        if (this.isControlledCamera()) {
            this.xxa = this.input.leftImpulse;
            this.zza = this.input.forwardImpulse;
            this.jumping = this.input.jumping;
            this.yBobO = this.yBob;
            this.xBobO = this.xBob;
            this.xBob = (float)((double)this.xBob + (double)(this.xRot - this.xBob) * 0.5D);
            this.yBob = (float)((double)this.yBob + (double)(this.yRot - this.yBob) * 0.5D);
        }

    }

    protected boolean isControlledCamera() {
        return this.minecraft.getCameraEntity() == this;
    }

    /**
     * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
     * react to sunlight and start to burn.
     */
    @Override
    public void aiStep() {
        ++this.sprintTime;
        if (this.sprintTriggerTime > 0) {
            --this.sprintTriggerTime;
        }

        this.handleNetherPortalClient();
        boolean flag = this.input.jumping;
        boolean flag1 = this.input.shiftKeyDown;
        boolean flag2 = this.hasEnoughImpulseToStartSprinting();
        this.crouching = !this.abilities.flying && !this.isSwimming() && this.canEnterPose(Pose.CROUCHING) && (this.isShiftKeyDown() || !this.isSleeping() && !this.canEnterPose(Pose.STANDING));
        this.input.tick(this.isMovingSlowly());
        net.minecraftforge.client.ForgeHooksClient.onInputUpdate(this, this.input);
        this.minecraft.getTutorial().onInput(this.input);
        if (this.isUsingItem() && !this.isPassenger()) {
            this.input.leftImpulse *= 0.2F;
            this.input.forwardImpulse *= 0.2F;
            this.sprintTriggerTime = 0;
        }

        boolean flag3 = false;
        if (this.autoJumpTime > 0) {
            --this.autoJumpTime;
            flag3 = true;
            this.input.jumping = true;
        }

        if (!this.noPhysics) {
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35D, this.getZ() + (double)this.getBbWidth() * 0.35D);
            this.moveTowardsClosestSpace(this.getX() - (double)this.getBbWidth() * 0.35D, this.getZ() - (double)this.getBbWidth() * 0.35D);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35D, this.getZ() - (double)this.getBbWidth() * 0.35D);
            this.moveTowardsClosestSpace(this.getX() + (double)this.getBbWidth() * 0.35D, this.getZ() + (double)this.getBbWidth() * 0.35D);
        }

        if (flag1) {
            this.sprintTriggerTime = 0;
        }

        boolean flag4 = (float)this.getFoodData().getFoodLevel() > 6.0F || this.abilities.mayfly;
        if ((this.onGround || this.isUnderWater()) && !flag1 && !flag2 && this.hasEnoughImpulseToStartSprinting() && !this.isSprinting() && flag4 && !this.isUsingItem() && !this.hasEffect(Effects.BLINDNESS)) {
            if (this.sprintTriggerTime <= 0 && !this.minecraft.options.keySprint.isDown()) {
                this.sprintTriggerTime = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && (!this.isInWater() || this.isUnderWater()) && this.hasEnoughImpulseToStartSprinting() && flag4 && !this.isUsingItem() && !this.hasEffect(Effects.BLINDNESS) && this.minecraft.options.keySprint.isDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting()) {
            boolean flag5 = !this.input.hasForwardImpulse() || !flag4;
            boolean flag6 = flag5 || this.horizontalCollision || this.isInWater() && !this.isUnderWater();
            if (this.isSwimming()) {
                if (!this.onGround && !this.input.shiftKeyDown && flag5 || !this.isInWater()) {
                    this.setSprinting(false);
                }
            } else if (flag6) {
                this.setSprinting(false);
            }
        }

        boolean flag7 = false;
        if (this.abilities.mayfly) {
            if (this.minecraft.gameMode.isAlwaysFlying()) {
                if (!this.abilities.flying) {
                    this.abilities.flying = true;
                    flag7 = true;
                    this.onUpdateAbilities();
                }
            } else if (!flag && this.input.jumping && !flag3) {
                if (this.jumpTriggerTime == 0) {
                    this.jumpTriggerTime = 7;
                } else if (!this.isSwimming()) {
                    this.abilities.flying = !this.abilities.flying;
                    flag7 = true;
                    this.onUpdateAbilities();
                    this.jumpTriggerTime = 0;
                }
            }
        }

//        if (this.input.jumping && !flag7 && !flag && !this.abilities.flying && !this.isPassenger() && !this.onClimbable()) {
//            ItemStack itemstack = this.getItemBySlot(EquipmentSlotType.CHEST);
//            if (itemstack.canElytraFly(this) && this.tryToStartFallFlying()) {
//                this.connection.send(new CEntityActionPacket(this, CEntityActionPacket.Action.START_FALL_FLYING));
//            }
//        }

        this.wasFallFlying = this.isFallFlying();
        if (this.isInWater() && this.input.shiftKeyDown && this.isAffectedByFluids()) {
            this.goDownInWater();
        }

        if (this.isEyeInFluid(FluidTags.WATER)) {
            int i = this.isSpectator() ? 10 : 1;
            this.waterVisionTime = MathHelper.clamp(this.waterVisionTime + i, 0, 600);
        } else if (this.waterVisionTime > 0) {
            this.isEyeInFluid(FluidTags.WATER);
            this.waterVisionTime = MathHelper.clamp(this.waterVisionTime - 10, 0, 600);
        }

        if (this.abilities.flying && this.isControlledCamera()) {
            int j = 0;
            if (this.input.shiftKeyDown) {
                --j;
            }

            if (this.input.jumping) {
                ++j;
            }

            if (j != 0) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)((float)j * this.abilities.getFlyingSpeed() * 3.0F), 0.0D));
            }
        }

        if (this.isRidingJumpable()) {
            IJumpingMount ijumpingmount = (IJumpingMount)this.getVehicle();
            if (this.jumpRidingTicks < 0) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks == 0) {
                    this.jumpRidingScale = 0.0F;
                }
            }

            if (flag && !this.input.jumping) {
                this.jumpRidingTicks = -10;
                ijumpingmount.onPlayerJump(MathHelper.floor(this.getJumpRidingScale() * 100.0F));
//                this.sendRidingJump();
            } else if (!flag && this.input.jumping) {
                this.jumpRidingTicks = 0;
                this.jumpRidingScale = 0.0F;
            } else if (flag) {
                ++this.jumpRidingTicks;
                if (this.jumpRidingTicks < 10) {
                    this.jumpRidingScale = (float)this.jumpRidingTicks * 0.1F;
                } else {
                    this.jumpRidingScale = 0.8F + 2.0F / (float)(this.jumpRidingTicks - 9) * 0.1F;
                }
            }
        } else {
            this.jumpRidingScale = 0.0F;
        }

        super.aiStep();
        if (this.onGround && this.abilities.flying && !this.minecraft.gameMode.isAlwaysFlying()) {
            this.abilities.flying = false;
            this.onUpdateAbilities();
        }

    }

    private void handleNetherPortalClient() {
        this.oPortalTime = this.portalTime;
        if (this.isInsidePortal) {
            if (this.minecraft.screen != null && !this.minecraft.screen.isPauseScreen()) {
                if (this.minecraft.screen instanceof ContainerScreen) {
                    this.closeContainer();
                }

                this.minecraft.setScreen((Screen)null);
            }

            if (this.portalTime == 0.0F) {
                this.minecraft.getSoundManager().play(SimpleSound.forLocalAmbience(SoundEvents.PORTAL_TRIGGER, this.random.nextFloat() * 0.4F + 0.8F, 0.25F));
            }

            this.portalTime += 0.0125F;
            if (this.portalTime >= 1.0F) {
                this.portalTime = 1.0F;
            }

            this.isInsidePortal = false;
        } else if (this.hasEffect(Effects.CONFUSION) && this.getEffect(Effects.CONFUSION).getDuration() > 60) {
            this.portalTime += 0.006666667F;
            if (this.portalTime > 1.0F) {
                this.portalTime = 1.0F;
            }
        } else {
            if (this.portalTime > 0.0F) {
                this.portalTime -= 0.05F;
            }

            if (this.portalTime < 0.0F) {
                this.portalTime = 0.0F;
            }
        }

        this.processPortalCooldown();
    }

    /**
     * Handles updating while riding another entity
     */
    @Override
    public void rideTick() {
        super.rideTick();
        if (this.wantsToStopRiding() && this.isPassenger()) this.input.shiftKeyDown = false;
        this.handsBusy = false;
        if (this.getVehicle() instanceof BoatEntity) {
            BoatEntity boatentity = (BoatEntity)this.getVehicle();
            boatentity.setInput(this.input.left, this.input.right, this.input.up, this.input.down);
            this.handsBusy |= this.input.left || this.input.right || this.input.up || this.input.down;
        }

    }

    public boolean isHandsBusy() {
        return this.handsBusy;
    }

    /**
     * Removes the given potion effect from the active potion map and returns it. Does not call cleanup callbacks for the
     * end of the potion effect.
     */
    @Nullable
    @Override
    public EffectInstance removeEffectNoUpdate(@Nullable Effect pEffect) {
        if (pEffect == Effects.CONFUSION) {
            this.oPortalTime = 0.0F;
            this.portalTime = 0.0F;
        }

        return super.removeEffectNoUpdate(pEffect);
    }

    @Override
    public void move(MoverType pType, Vector3d pPos) {
        double d0 = this.getX();
        double d1 = this.getZ();
        super.move(pType, pPos);
        this.updateAutoJump((float)(this.getX() - d0), (float)(this.getZ() - d1));
    }

    public boolean isAutoJumpEnabled() {
        return this.autoJumpEnabled;
    }

    protected void updateAutoJump(float pMovementX, float pMovementZ) {
        if (this.canAutoJump()) {
            Vector3d vector3d = this.position();
            Vector3d vector3d1 = vector3d.add((double)pMovementX, 0.0D, (double)pMovementZ);
            Vector3d vector3d2 = new Vector3d((double)pMovementX, 0.0D, (double)pMovementZ);
            float f = this.getSpeed();
            float f1 = (float)vector3d2.lengthSqr();
            if (f1 <= 0.001F) {
                Vector2f vector2f = this.input.getMoveVector();
                float f2 = f * vector2f.x;
                float f3 = f * vector2f.y;
                float f4 = MathHelper.sin(this.yRot * ((float)Math.PI / 180F));
                float f5 = MathHelper.cos(this.yRot * ((float)Math.PI / 180F));
                vector3d2 = new Vector3d((double)(f2 * f5 - f3 * f4), vector3d2.y, (double)(f3 * f5 + f2 * f4));
                f1 = (float)vector3d2.lengthSqr();
                if (f1 <= 0.001F) {
                    return;
                }
            }

            float f12 = MathHelper.fastInvSqrt(f1);
            Vector3d vector3d12 = vector3d2.scale((double)f12);
            Vector3d vector3d13 = this.getForward();
            float f13 = (float)(vector3d13.x * vector3d12.x + vector3d13.z * vector3d12.z);
            if (!(f13 < -0.15F)) {
                ISelectionContext iselectioncontext = ISelectionContext.of(this);
                BlockPos blockpos = new BlockPos(this.getX(), this.getBoundingBox().maxY, this.getZ());
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (blockstate.getCollisionShape(this.level, blockpos, iselectioncontext).isEmpty()) {
                    blockpos = blockpos.above();
                    BlockState blockstate1 = this.level.getBlockState(blockpos);
                    if (blockstate1.getCollisionShape(this.level, blockpos, iselectioncontext).isEmpty()) {
                        float f7 = 1.2F;
                        if (this.hasEffect(Effects.JUMP)) {
                            f7 += (float)(this.getEffect(Effects.JUMP).getAmplifier() + 1) * 0.75F;
                        }

                        float f8 = Math.max(f * 7.0F, 1.0F / f12);
                        Vector3d vector3d4 = vector3d1.add(vector3d12.scale((double)f8));
                        float f9 = this.getBbWidth();
                        float f10 = this.getBbHeight();
                        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(vector3d, vector3d4.add(0.0D, (double)f10, 0.0D))).inflate((double)f9, 0.0D, (double)f9);
                        Vector3d lvt_19_1_ = vector3d.add(0.0D, (double)0.51F, 0.0D);
                        vector3d4 = vector3d4.add(0.0D, (double)0.51F, 0.0D);
                        Vector3d vector3d5 = vector3d12.cross(new Vector3d(0.0D, 1.0D, 0.0D));
                        Vector3d vector3d6 = vector3d5.scale((double)(f9 * 0.5F));
                        Vector3d vector3d7 = lvt_19_1_.subtract(vector3d6);
                        Vector3d vector3d8 = vector3d4.subtract(vector3d6);
                        Vector3d vector3d9 = lvt_19_1_.add(vector3d6);
                        Vector3d vector3d10 = vector3d4.add(vector3d6);
                        Iterator<AxisAlignedBB> iterator = this.level.getCollisions(this, axisalignedbb, (p_239205_0_) -> {
                            return true;
                        }).flatMap((p_212329_0_) -> {
                            return p_212329_0_.toAabbs().stream();
                        }).iterator();
                        float f11 = Float.MIN_VALUE;

                        while(iterator.hasNext()) {
                            AxisAlignedBB axisalignedbb1 = iterator.next();
                            if (axisalignedbb1.intersects(vector3d7, vector3d8) || axisalignedbb1.intersects(vector3d9, vector3d10)) {
                                f11 = (float)axisalignedbb1.maxY;
                                Vector3d vector3d11 = axisalignedbb1.getCenter();
                                BlockPos blockpos1 = new BlockPos(vector3d11);

                                for(int i = 1; (float)i < f7; ++i) {
                                    BlockPos blockpos2 = blockpos1.above(i);
                                    BlockState blockstate2 = this.level.getBlockState(blockpos2);
                                    VoxelShape voxelshape;
                                    if (!(voxelshape = blockstate2.getCollisionShape(this.level, blockpos2, iselectioncontext)).isEmpty()) {
                                        f11 = (float)voxelshape.max(Direction.Axis.Y) + (float)blockpos2.getY();
                                        if ((double)f11 - this.getY() > (double)f7) {
                                            return;
                                        }
                                    }

                                    if (i > 1) {
                                        blockpos = blockpos.above();
                                        BlockState blockstate3 = this.level.getBlockState(blockpos);
                                        if (!blockstate3.getCollisionShape(this.level, blockpos, iselectioncontext).isEmpty()) {
                                            return;
                                        }
                                    }
                                }
                                break;
                            }
                        }

                        if (f11 != Float.MIN_VALUE) {
                            float f14 = (float)((double)f11 - this.getY());
                            if (!(f14 <= 0.5F) && !(f14 > f7)) {
                                this.autoJumpTime = 1;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canAutoJump() {
        return this.isAutoJumpEnabled() && this.autoJumpTime <= 0 && this.onGround && !this.isStayingOnGroundSurface() && !this.isPassenger() && this.isMoving() && (double)this.getBlockJumpFactor() >= 1.0D;
    }

    private boolean isMoving() {
        Vector2f vector2f = this.input.getMoveVector();
        return vector2f.x != 0.0F || vector2f.y != 0.0F;
    }

    private boolean hasEnoughImpulseToStartSprinting() {
        return this.isUnderWater() ? this.input.hasForwardImpulse() : (double)this.input.forwardImpulse >= 0.8;
    }

    public float getWaterVision() {
        if (!this.isEyeInFluid(FluidTags.WATER)) {
            return 0.0F;
        } else {
            if (this.waterVisionTime >= 600) {
                return 1.0F;
            } else {
                float f2 = MathHelper.clamp((float)this.waterVisionTime / 100.0F, 0.0F, 1.0F);
                float f3 = (float)this.waterVisionTime < 100.0F ? 0.0F : MathHelper.clamp(((float)this.waterVisionTime - 100.0F) / 500.0F, 0.0F, 1.0F);
                return f2 * 0.6F + f3 * 0.39999998F;
            }
        }
    }

    @Override
    public boolean isUnderWater() {
        return this.wasUnderwater;
    }

    @Override
    protected boolean updateIsUnderwater() {
        boolean flag = this.wasUnderwater;
        boolean flag1 = super.updateIsUnderwater();
        if (this.isSpectator()) {
            return this.wasUnderwater;
        } else {
            if (!flag && flag1) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
//                this.minecraft.getSoundManager().play(new UnderwaterAmbientSounds.UnderWaterSound(this));
            }

            if (flag && !flag1) {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.AMBIENT_UNDERWATER_EXIT, SoundCategory.AMBIENT, 1.0F, 1.0F, false);
            }

            return this.wasUnderwater;
        }
    }

    @Override
    public Vector3d getRopeHoldPosition(float pPartialTicks) {
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            float f = MathHelper.lerp(pPartialTicks * 0.5F, this.yRot, this.yRotO) * ((float)Math.PI / 180F);
            float f1 = MathHelper.lerp(pPartialTicks * 0.5F, this.xRot, this.xRotO) * ((float)Math.PI / 180F);
            double d0 = this.getMainArm() == HandSide.RIGHT ? -1.0D : 1.0D;
            Vector3d vector3d = new Vector3d(0.39D * d0, -0.6D, 0.3D);
            return vector3d.xRot(-f1).yRot(-f).add(this.getEyePosition(pPartialTicks));
        } else {
            return super.getRopeHoldPosition(pPartialTicks);
        }
    }
}
