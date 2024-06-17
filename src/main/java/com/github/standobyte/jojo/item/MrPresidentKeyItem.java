package com.github.standobyte.jojo.item;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.standskin.StandSkinsManager;
import com.github.standobyte.jojo.power.impl.stand.StandInstance;
import com.github.standobyte.jojo.power.impl.stand.StandInstance.StandPart;
import com.github.standobyte.jojo.world.dimension.ModDimensions;
import com.github.standobyte.jojo.world.dimension.mr_president.MrPresidentInsideTeleporter;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

public class MrPresidentKeyItem extends Item {
    public final boolean masterKey;

    public MrPresidentKeyItem(Properties pProperties, boolean masterKey) {
        super(pProperties);
        this.masterKey = masterKey;
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
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (masterKey) {
            tooltip.add(new TranslationTextComponent(getOrCreateDescriptionId() + ".hint").withStyle(TextFormatting.GRAY));
            tooltip.add(new TranslationTextComponent("item.jojo.creative_only_tooltip").withStyle(TextFormatting.DARK_GRAY));
        }
    }

}
