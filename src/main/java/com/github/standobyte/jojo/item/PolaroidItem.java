package com.github.standobyte.jojo.item;

import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.polaroid.PolaroidHelper;
import com.github.standobyte.jojo.util.mc.MCUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class PolaroidItem extends Item {

    public PolaroidItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity cameraPlayer, Hand hand) {
        ItemStack stack = cameraPlayer.getItemInHand(hand);
        
        if (!cameraPlayer.abilities.instabuild) {
            ItemStack paperItem = ItemStack.EMPTY;
            for (int i = 0; i < cameraPlayer.inventory.getContainerSize(); ++i) {
                ItemStack item = cameraPlayer.inventory.getItem(i);
                if (!item.isEmpty() && item.getItem() == Items.PAPER) {
                    paperItem = item;
                    break;
                }
            }
            
            if (paperItem.isEmpty()) {
                if (!world.isClientSide()) {
                    ((ServerPlayerEntity) cameraPlayer).displayClientMessage(new TranslationTextComponent("jojo.polaroid.paper"), true);
                }
                return ActionResult.fail(stack);
            }
            
            if (!world.isClientSide()) {
                paperItem.shrink(1);
            }
        }
        
//        /* Other classes necessary:
//         *   network.packets.fromserver.PhotoForOtherPlayerPacket - makes the client of other player we're taking a spiritual photo of take the screenshot for us
//         *   network.PacketManager - register the network packet
//         */
//        
//        // can be a condition for the stand action
//        boolean hasOtherPlayers = world.isClientSide() ? ClientUtil.hasOtherPlayers() : ((ServerWorld) world).getServer().getPlayerCount() > 1;
//        if (!world.isClientSide() && hasOtherPlayers) {
//            Vector3d lookVec = cameraPlayer.getLookAngle();
//            Optional<ServerPlayerEntity> playerToDox = ((ServerWorld) world).getServer().getPlayerList().getPlayers()
//                    // as an example, here we choose the player that is...
//                    .stream()
//                    // ...not ourselves
//                    .filter(player -> player != cameraPlayer)
//                    // ...is in the same dimension as us
//                    .filter(player -> player.level == world)
//                    // ...a Stand user
//                    .filter(player -> IStandPower.getStandPowerOptional(player).map(IStandPower::hasPower).orElse(false))
//                    // ...and is the closest to our look direction, no matter the distance
//                    .max(Comparator.comparingDouble(player -> {
//                        Vector3d vecToPlayer = player.position().subtract(cameraPlayer.position()).normalize();
//                        return vecToPlayer.dot(lookVec);
//                    }));
//            playerToDox.ifPresent(player -> {
//                PacketManager.sendToClient(new PhotoForOtherPlayerPacket(cameraPlayer.getId()), player);
//            });
//            if (!cameraPlayer.abilities.instabuild) { // or if you're DIO and can do the same thing using Jonathan's Stand and apparently not break the camera :rolling_eyes:
//                stack.shrink(1);
//                cameraPlayer.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
//            }
//            return ActionResult.consume(stack);
//        }
        
        if (world.isClientSide()) {
            switch (MCUtil.getHandSide(cameraPlayer, hand)) {
            case RIGHT:
                PolaroidHelper.takePicture(null, null, false, cameraPlayer.getId());
                break;
            case LEFT:
                float xRot = Math.max(-cameraPlayer.xRot, -82.5f);
                float yRot = cameraPlayer.yRot;
                float xRotAmount = Math.abs(xRot / 90);
                cameraPlayer.setYBodyRot(yRot + 45 * (1 - xRotAmount));
                cameraPlayer.yBodyRotO = cameraPlayer.yBodyRot;
                Vector3d lookVec = cameraPlayer.getLookAngle();
                Vector3d headUpVec = lookVec.xRot(90);
                Vector3d cameraPos = new Vector3d(cameraPlayer.getX(), cameraPlayer.getEyeY() - 0.1, cameraPlayer.getZ());
                cameraPos = cameraPos.add(headUpVec.scale(xRotAmount * 0.2));
                cameraPos = cameraPos.add(lookVec.scale((1 - xRotAmount * 0.2)));
                PolaroidHelper.takePicture(cameraPos, 
                        angle -> new Vector3f(xRot, yRot - 165, 0), false, cameraPlayer.getId());
                break;
            }
        }
        else {
            cameraPlayer.getCooldowns().addCooldown(this, 60);
        }
        
        return ActionResult.consume(stack);
    }
    
    
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        ClientUtil.addItemReferenceQuote(tooltip, this);
        tooltip.add(ClientUtil.donoItemTooltip("August_dr"));
    }
}
