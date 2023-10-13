package com.github.standobyte.jojo.util.mc;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrEntitySpecialEffectPacket;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class CustomVillagerTrades {
    private static final String ALREADY_GAVE_TRADE_TAG = "JojoUniqueTrade";
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (target instanceof VillagerEntity
                && !target.level.isClientSide()
                && !target.getTags().contains(ALREADY_GAVE_TRADE_TAG)
                && giveTradeManually((VillagerEntity) target, event.getPlayer())) {
            target.addTag(ALREADY_GAVE_TRADE_TAG);
        }
    }
    
    
    public static enum MapTrade {
        METEORITE_MAP(
                new EmeraldForMapTrade(16, ModStructures.METEORITE, MapDecoration.Type.TARGET_POINT, 0x6d6bb9, 1, 15), 
                240000,
                VillagerType.SNOW),
        HAMON_MAP(
                new EmeraldForMapTrade(24, ModStructures.HAMON_TEMPLE, MapDecoration.Type.TARGET_POINT, 0x474747, 1, 23), 
                480000, 
                VillagerType.TAIGA),
        PILLARMAN_MAP(
                new EmeraldForMapTrade(32, ModStructures.PILLARMAN_TEMPLE, MapDecoration.Type.TARGET_POINT, 0x508d50, 1, 30), 
                960000, 
                VillagerType.JUNGLE);
        
        final ITrade trade;
        public final long tradeCooldownTicks;
        final VillagerType villagerFamiliar;
        
        private MapTrade(ITrade trade, long cooldownTicks, VillagerType villagerFamiliar) {
            this.trade = trade;
            this.tradeCooldownTicks = cooldownTicks;
            this.villagerFamiliar = villagerFamiliar;
        }
        
        FamiliarWith villagerFamiliarWith(VillagerType villager) {
            Optional<MapTrade> familiarWithBiome = Arrays.stream(MapTrade.values())
                    .filter(mapType -> mapType.villagerFamiliar == villager)
                    .findFirst();
            if (familiarWithBiome.isPresent()) {
                return familiarWithBiome.get() == this ? FamiliarWith.THIS_BIOME : FamiliarWith.OTHER_BIOME;
            }
            else {
                return FamiliarWith.NO_BIOME;
            }
        }
        
        public static enum FamiliarWith {
            THIS_BIOME,
            OTHER_BIOME,
            NO_BIOME
        }
    }
    
    private static boolean giveTradeManually(VillagerEntity villager, PlayerEntity player) {
        VillagerData villagerData = villager.getVillagerData();
        VillagerProfession profession = villagerData.getProfession();
        PlayerUtilCap playerTradeCooldowns = player.getCapability(PlayerUtilCapProvider.CAPABILITY).orElse(null);
        if (playerTradeCooldowns == null) {
            return false;
        }
        
        if (profession == VillagerProfession.CARTOGRAPHER && villagerData.getLevel() >= 4) {
            boolean playerHasStand = IStandPower.getStandPowerOptional(player).map(IStandPower::hasPower).orElse(true);
            boolean playerHasHamon = INonStandPower.getNonStandPowerOptional(player).map(power -> power.getType() == ModPowers.HAMON.get()).orElse(true);
            boolean playerHasVampirism = INonStandPower.getNonStandPowerOptional(player).map(power -> power.getType() == ModPowers.VAMPIRISM.get()).orElse(true);
            
            double meteoriteMapChance;
            if (playerHasStand)                         meteoriteMapChance = 0;
            else if (playerHasVampirism)                meteoriteMapChance = 0.04;
            else if (playerHasHamon)                    meteoriteMapChance = 0.1;
            
            else switch (MapTrade.METEORITE_MAP.villagerFamiliarWith(villagerData.getType())) {
            case /*FamiliarWith.*/THIS_BIOME:           meteoriteMapChance = 1;
                break;
            case /*FamiliarWith.*/OTHER_BIOME:          meteoriteMapChance = 0;
                break;
            case /*FamiliarWith.*/NO_BIOME:             meteoriteMapChance = 0.8;
                break;
            default:
                throw new NoSuchElementException();
            }
            
            if (addMapTrade(meteoriteMapChance, 
                    villager, MapTrade.METEORITE_MAP, 
                    playerTradeCooldowns, player)) {
                return true;
            }
            
            
            double hamonTempleMapChance;
            if (playerHasHamon || playerHasVampirism)   hamonTempleMapChance = 0;
            else if (playerHasStand)                    hamonTempleMapChance = 0.0625;
            else switch (MapTrade.HAMON_MAP.villagerFamiliarWith(villagerData.getType())) {
            case /*FamiliarWith.*/THIS_BIOME:           hamonTempleMapChance = 0.75;
                break;
            case /*FamiliarWith.*/OTHER_BIOME:          hamonTempleMapChance = 0;
                break;
            case /*FamiliarWith.*/NO_BIOME:             hamonTempleMapChance = 0.5;
                break;
            default:
                throw new NoSuchElementException();
            }
            
            if (addMapTrade(hamonTempleMapChance, 
                    villager, MapTrade.HAMON_MAP, 
                    playerTradeCooldowns, player)) {
                return true;
            }
            
            
            double pillarManTempleMapChance;
            if (playerHasHamon || playerHasVampirism)   pillarManTempleMapChance = 0;
            else if (playerHasStand)                    pillarManTempleMapChance = 0.0125;
            else switch (MapTrade.PILLARMAN_MAP.villagerFamiliarWith(villagerData.getType())) {
            case /*FamiliarWith.*/THIS_BIOME:           pillarManTempleMapChance = 0.5;
                break;
            case /*FamiliarWith.*/OTHER_BIOME:          pillarManTempleMapChance = 0;
                break;
            case /*FamiliarWith.*/NO_BIOME:             pillarManTempleMapChance = 0.25;
                break;
            default:
                throw new NoSuchElementException();
            }
            
            if (addMapTrade(pillarManTempleMapChance, 
                    villager, MapTrade.PILLARMAN_MAP, 
                    playerTradeCooldowns, player)) {
                return true;
            }
        }
        return false;
    }
    
    
    
    private static boolean addMapTrade(double randomChance,
            AbstractVillagerEntity trader, MapTrade tradeType, 
            PlayerUtilCap cooldownsCap, PlayerEntity player) {
        if (randomChance <= 0) {
            return false;
        }
        
        if (cooldownsCap.canTradeNow(tradeType, trader.level)) {
            if (player.getRandom().nextDouble() < randomChance) {
                MerchantOffer offer = tradeType.trade.getOffer(trader, trader.getRandom());
                if (offer != null) {
                    cooldownsCap.setTradeTime(tradeType, trader.level);
                    trader.getOffers().add(offer);
                    return true;
                }
            }
            else {
                cooldownsCap.setTradeTime(tradeType, trader.level);
            }
        }
        
        return false;
    }
    
    // the VillagerTrades.ITrade implementation classes are package private in the vanilla code
    static class EmeraldForMapTrade implements VillagerTrades.ITrade {
        private final int emeraldCost;
        private final Supplier<? extends Structure<?>> destination;
        private final MapDecoration.Type destinationType;
        private final int customColor;
        private final int maxUses;
        private final int villagerXp;

        public EmeraldForMapTrade(int pEmeraldCost, Supplier<? extends Structure<?>> pDestination, 
                MapDecoration.Type pDestinationType, int customColor, int pMaxUses, int pVillagerXp) {
            this.emeraldCost = pEmeraldCost;
            this.destination = pDestination;
            this.destinationType = pDestinationType;
            this.customColor = customColor;
            this.maxUses = pMaxUses;
            this.villagerXp = pVillagerXp;
        }

        @Nullable
        public MerchantOffer getOffer(Entity pTrader, Random pRand) {
            if (!(pTrader.level instanceof ServerWorld)) {
                return null;
            } else {
                ServerWorld serverworld = (ServerWorld)pTrader.level;
                Structure<?> structure = destination.get();
                BlockPos blockpos = serverworld.findNearestMapFeature(structure, pTrader.blockPosition(), 100, true);
                if (blockpos != null) {
                    ItemStack itemstack = FilledMapItem.create(serverworld, blockpos.getX(), blockpos.getZ(), (byte)2, true, true);
                    FilledMapItem.renderBiomePreviewMap(serverworld, itemstack);
                    MapData.addTargetDecoration(itemstack, blockpos, "+", this.destinationType);
                    if (customColor > 0) {
                        CompoundNBT compoundnbt1 = itemstack.getOrCreateTagElement("display");
                        compoundnbt1.putInt("MapColor", customColor);
                    }
                    itemstack.setHoverName(new TranslationTextComponent("filled_map." + structure.getFeatureName().toLowerCase(Locale.ROOT)));
                    return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(Items.COMPASS), itemstack, this.maxUses, this.villagerXp, 0.2F);
                } else {
                    return null;
                }
            }
        }
    }
    
    // the initialization is temporarily commented out in PlayerUtilCap's constructor
    public static class MapItemStackTradeListener extends PlayerStatListener<Item> { // TODO make tracking bought items ItemStack sensitive

        private static final ITextComponent[] MAP_NAMES = {
                new TranslationTextComponent("filled_map.jojo:meteorite"),
                new TranslationTextComponent("filled_map.jojo:hamon_temple"),
                new TranslationTextComponent("filled_map.jojo:pillarman_temple")
        };
        private static final TrEntitySpecialEffectPacket.Type[] VISUALS_TYPE = {
                TrEntitySpecialEffectPacket.Type.SOLD_METEORITE_MAP,
                TrEntitySpecialEffectPacket.Type.SOLD_HAMON_TEMPLE_MAP,
                TrEntitySpecialEffectPacket.Type.SOLD_PILLAR_MAN_TEMPLE_MAP
        };
        
        public MapItemStackTradeListener(ServerPlayerEntity player) {
            super(Stats.ITEM_CRAFTED.get(Items.FILLED_MAP), player);
        }

        @Override
        protected void handleChanged(Item map, ServerPlayerEntity player, long oldVal, long newVal) {
//            List<ItemStack> mapItems = player.inventory.items.stream() // FIXME the item is not in the inventory by this time yet, so it doesn't actually work
//                    .filter(item -> !item.isEmpty() && item.getItem() == map).collect(Collectors.toList())
            List<ItemStack> mapItems = player.inventoryMenu.slots.stream()
                    .map(Slot::getItem)
                    .filter(item -> !item.isEmpty() && item.getItem() == map).collect(Collectors.toList()); // FIXME nope, doesn't work either
            for (ItemStack mapItem : mapItems) {
                for (int i = 0; i < MAP_NAMES.length; i++) {
                    ITextComponent mapName = MAP_NAMES[i];
                    if (mapName.equals(mapItem.getHoverName())) {
                        Optional<Entity> trader = getNpcTradingWith(player);
                        if (trader.isPresent()) {
                            Entity traderEntity = trader.get();
                            TrEntitySpecialEffectPacket.Type visualsType = VISUALS_TYPE[i];
                            
                            player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                                cap.doWhen(
                                        () -> PacketManager.sendToClientsTrackingAndSelf(new TrEntitySpecialEffectPacket(
                                                traderEntity.getId(), visualsType, player.getId()), traderEntity), 
                                        () -> player.containerMenu == player.inventoryMenu);
                            });
                            
                            // TODO add the visuals and the sound after the player closes the screen (packet handler)
                            // TODO add as notification (to trigger it only once per game)
                        }
                        else {
                            return;
                        }
                    }
                }
            }
        }
    }
    
    
    
    public static Optional<Entity> getNpcTradingWith(ServerPlayerEntity player) {
        if (player.containerMenu instanceof MerchantContainer) {
            MerchantContainer tradingMenu = (MerchantContainer) player.containerMenu;
            if (tradingMenu.stillValid(player)) {
                IMerchant trader = CommonReflection.getTrader(tradingMenu);
                if (trader instanceof Entity) {
                    return Optional.of((Entity) trader);
                }
            }
        }
        
        return Optional.empty();
    }
}
