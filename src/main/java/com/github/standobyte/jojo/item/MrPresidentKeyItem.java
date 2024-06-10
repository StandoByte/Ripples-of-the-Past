package com.github.standobyte.jojo.item;

import java.util.UUID;

import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.github.standobyte.jojo.world.dimension.mr_president.MrPresidentInsideTeleporter;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentKeyItem extends Item {

    public MrPresidentKeyItem(Properties pProperties) {
        super(pProperties);
    }
    
//    @Override
//    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
//        ItemStack item = player.getItemInHand(hand);
//        if (!world.isClientSide()) {
//            if (item.hasTag() && item.getTag().hasUUID("TurtleEntity")) {
//                UUID turtleId = item.getTag().getUUID("TurtleEntity");
//                MinecraftServer server = world.getServer();
//                ServerWorld mrPresidentWorld = server.getLevel(ModDimensions.MR_PRESIDENT);
//                if (mrPresidentWorld != null) {
//                    ITeleporter teleporter = new MrPresidentInsideTeleporter(turtleId);
//                    
//                    server.tell(new TickDelayedTask(server.getTickCount(), () -> {
//                        player.changeDimension(mrPresidentWorld, teleporter);
//                    }));
//                }
//            }
//        }
//        return ActionResult.consume(item);
//    }

}
