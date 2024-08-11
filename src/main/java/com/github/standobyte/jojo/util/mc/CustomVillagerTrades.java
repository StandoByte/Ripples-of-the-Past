package com.github.standobyte.jojo.util.mc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoMod;
import com.github.standobyte.jojo.capability.entity.MerchantData;
import com.github.standobyte.jojo.capability.entity.MerchantDataProvider;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCap;
import com.github.standobyte.jojo.capability.entity.PlayerUtilCapProvider;
import com.github.standobyte.jojo.init.ModSounds;
import com.github.standobyte.jojo.init.ModStructures;
import com.github.standobyte.jojo.init.power.non_stand.ModPowers;
import com.github.standobyte.jojo.network.PacketManager;
import com.github.standobyte.jojo.network.packets.fromserver.TrEntitySpecialEffectPacket;
import com.github.standobyte.jojo.power.IPower;
import com.github.standobyte.jojo.power.impl.nonstand.INonStandPower;
import com.github.standobyte.jojo.power.impl.nonstand.type.NonStandPowerType;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.power.impl.stand.type.StandType;
import com.github.standobyte.jojo.util.mc.reflection.CommonReflection;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
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
    private static final boolean DEBUG = false;
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        if (target instanceof VillagerEntity && !target.level.isClientSide()) {
            target.getCapability(MerchantDataProvider.CAPABILITY).ifPresent(merchantData -> {
                if (!merchantData.gaveUniqueTrade()
                        && giveTradeManually((VillagerEntity) target, event.getPlayer(), merchantData)) {
                    merchantData.setGaveUniqueTrade();
                }
            });
        }
    }
    
    private static boolean giveTradeManually(VillagerEntity villager, PlayerEntity player, MerchantData merchantData) {
        VillagerData villagerData = villager.getVillagerData();
        VillagerProfession profession = villagerData.getProfession();
        
        if (profession == VillagerProfession.CARTOGRAPHER && (DEBUG || villagerData.getLevel() >= 4) && 
                !merchantData.getPlayerTriedTrading(player, MapTrades.TRIED_BUYING_EXPERT_MAP)) {
            merchantData.setPlayerTriedTrading(player, MapTrades.TRIED_BUYING_EXPERT_MAP);
            StandType<?> standType = IStandPower.getStandPowerOptional(player).resolve().map(IPower::getType).orElse(null);
            NonStandPowerType<?> powerType = INonStandPower.getNonStandPowerOptional(player).resolve().map(IPower::getType).orElse(null);
            List<MapTrades.MapTrade> maps = MapTrades.MapTrade.values();
            List<MapTrades.MapTrade> order = new ArrayList<>(maps);
            Collections.shuffle(order);
            
            for (MapTrades.MapTrade mapTrade : order) {
                double mapChance = mapTrade.getMapChance(standType, powerType, villagerData);
                // TODO add map trade chance event?
                if (addTrade(mapChance, villager, mapTrade.trade, 
                        player, merchantData, null /* the tag is null here because we've checked and set the TRIED_BUYING_MAP tag already */)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public static void onTrade(PlayerEntity player, ItemStack stack, 
            MerchantInventory slots, MerchantOffer offer) {
        if (stack.isEmpty()) return;
        
        if (stack.getItem() == Items.FILLED_MAP && stack.hasCustomHoverName()) {
            for (MapTrades.MapTrade trade : MapTrades.MapTrade.values()) {
                if (trade.mapName != null && trade.mapName.equals(stack.getHoverName())) {
                    trade.onTrade(player, stack, slots, offer);
                    break;
                }
            }
        }
    }
    
    
    
    public static class MapTrades {
        
        public abstract static class MapTrade {
            private static final Multimap<VillagerType, MapTrade> PER_BIOME = ArrayListMultimap.create();
            private static final List<MapTrade> VALUES = new ArrayList<>();
            
            // Probabilities of map trades
            
            public static final MapTrade METEORITE_MAP = new MapTrade(VillagerType.SNOW, "meteorite_map",
                    new EmeraldForMapTrade(16, ModStructures.METEORITE, MapDecoration.Type.TARGET_POINT, 0x6d6bb9, 1, 15),
                    new TranslationTextComponent("filled_map.jojo:meteorite"), ModSounds.MAP_BOUGHT_METEORITE.get(), PlayerUtilCap.OneTimeNotification.BOUGHT_METEORITE_MAP) {
                @Override
                public double getMapChance(@Nullable StandType<?> standType, @Nullable NonStandPowerType<?> powerType, VillagerData villager) {
                    double meteoriteMapChance;

                    if (standType != null)                                  meteoriteMapChance = 0;
                    else {
                        VillagerType biome = villager.getType();
                        if (biome == VillagerType.SNOW)                     meteoriteMapChance = 1;
                        else if (biomeHasOtherStructure(biome))             meteoriteMapChance = 0;
                        else if (canGiveMap(biome, VillagerType.SNOW))      meteoriteMapChance = 0.8;
                        else                                                meteoriteMapChance = 0.2;

                        if (powerType == ModPowers.VAMPIRISM.get())         meteoriteMapChance *= 0.05;
                        else if (powerType == ModPowers.HAMON.get())        meteoriteMapChance *= 0.0625;
                    }

                    return meteoriteMapChance;
                }
            };
            
            public static final MapTrade HAMON_MAP = new MapTrade(VillagerType.TAIGA, "hamon_map",
                    new EmeraldForMapTrade(24, ModStructures.HAMON_TEMPLE, MapDecoration.Type.TARGET_POINT, 0x474747, 1, 23),
                    new TranslationTextComponent("filled_map.jojo:hamon_temple"), ModSounds.MAP_BOUGHT_HAMON_TEMPLE.get(), PlayerUtilCap.OneTimeNotification.BOUGHT_HAMON_TEMPLE_MAP) {
                @Override
                public double getMapChance(@Nullable StandType<?> standType, @Nullable NonStandPowerType<?> powerType, VillagerData villager) {
                    double hamonTempleMapChance;

                    VillagerType biome = villager.getType();
                    if (biome == VillagerType.TAIGA)                        hamonTempleMapChance = 1;
                    else if (biomeHasOtherStructure(biome))                 hamonTempleMapChance = 0;
                    else if (canGiveMap(biome, VillagerType.TAIGA))         hamonTempleMapChance = 0.5;
                    else                                                    hamonTempleMapChance = 0.125;

                    if (standType != null)                                  hamonTempleMapChance *= 0.0625;
                    if (powerType == ModPowers.VAMPIRISM.get())             hamonTempleMapChance *= 0.125;
                    else if (powerType == ModPowers.HAMON.get())            hamonTempleMapChance *= 0.25;

                    return hamonTempleMapChance;
                }
            };
            
            public static final MapTrade PILLARMAN_MAP = new MapTrade(VillagerType.JUNGLE, "pillarman_map",
                    new EmeraldForMapTrade(32, ModStructures.PILLARMAN_TEMPLE, MapDecoration.Type.TARGET_POINT, 0x508d50, 1, 30),
                    new TranslationTextComponent("filled_map.jojo:pillarman_temple"), ModSounds.MAP_BOUGHT_PILLAR_MAN_TEMPLE.get(), PlayerUtilCap.OneTimeNotification.BOUGHT_PILLAR_MAN_TEMPLE_MAP) {
                @Override
                public double getMapChance(@Nullable StandType<?> standType, @Nullable NonStandPowerType<?> powerType, VillagerData villager) {
                    double pillarManTempleMapChance;

                    if (powerType == ModPowers.VAMPIRISM.get())             pillarManTempleMapChance = 0;
                    else {
                        VillagerType biome = villager.getType();
                        if (biome == VillagerType.JUNGLE)                   pillarManTempleMapChance = 1;
                        else if (biomeHasOtherStructure(biome))             pillarManTempleMapChance = 0;
                        else if (canGiveMap(biome, VillagerType.JUNGLE))    pillarManTempleMapChance = 0.25;
                        else                                                pillarManTempleMapChance = 0;

                        if (standType != null)                              pillarManTempleMapChance *= 0.05;
                        else if (powerType == ModPowers.HAMON.get())        pillarManTempleMapChance *= 0.4;
                    }

                    return pillarManTempleMapChance;
                }
            };
            
            /*
             *   Villager Biome      Meteorite   Hamon Temple    Pillar Man Temple
             *      Desert              0.2         0.125               0.25            // 0.475 to roll any map
             *      Jungle              0           0                   1               // 
             *      Plains              0.2         0.5                 0               // 0.6 to roll any map
             *      Savanna             0.2         0.125               0.25            // same as Desert
             *      Snow                1           0                   0               // 
             *      Swamp               0.8         0.5                 0.25            // 0.925 to roll any map
             *      Taiga               0           1                   0               // 
             */     
            
            
            
            public final ITrade trade;
            @Nullable public final VillagerType biome;
            public final String name;
            public final ITextComponent mapName;
            public final SoundEvent onFirstBuyFlavorSound;
            public final PlayerUtilCap.OneTimeNotification playerNotification;
            
            public MapTrade(@Nullable VillagerType biome, String name, EmeraldForMapTrade trade,
                    ITextComponent mapName, SoundEvent onFirstBuyFlavorSound, PlayerUtilCap.OneTimeNotification playerNotification) {
                this.trade = trade;
                trade.destinationType = this;
                this.name = name;
                
                VALUES.add(this);
                this.biome = biome;
                if (biome != null) {
                    PER_BIOME.put(biome, this);
                }
                
                this.mapName = mapName;
                this.onFirstBuyFlavorSound = onFirstBuyFlavorSound;
                this.playerNotification = playerNotification;
            }
            
            public static List<MapTrade> values() {
                return VALUES;
            }
            
            public void onTrade(PlayerEntity player, ItemStack stack, MerchantInventory slots, MerchantOffer offer) {
                if (playerNotification == null || onFirstBuyFlavorSound == null) return;
                
                Optional<PlayerUtilCap> playerNotifications = player.getCapability(PlayerUtilCapProvider.CAPABILITY).resolve();
                if (DEBUG || playerNotifications.map(notif -> !notif.sentNotification(playerNotification)).orElse(false)) {
                    playerNotifications.get().setSentNotification(playerNotification, true);
                    
                    IMerchant merchant = CommonReflection.getMerchant(slots);
                    if (merchant instanceof Entity) {
                        Entity merchantEntity = (Entity) merchant;
                        player.getCapability(PlayerUtilCapProvider.CAPABILITY).ifPresent(cap -> {
                            cap.doWhen(
                                    () -> PacketManager.sendToClientsTrackingAndSelf(new TrEntitySpecialEffectPacket(
                                            merchantEntity.getId(), onFirstBuyFlavorSound, player.getId()), merchantEntity), 
                                    () -> player.containerMenu == player.inventoryMenu);
                        });
                    }
                }
            }
            
            
            
            public abstract double getMapChance(@Nullable StandType<?> standType, @Nullable NonStandPowerType<?> powerType, VillagerData villager);
        }
        
        
        private static final String TRIED_BUYING_EXPERT_MAP = "ExpertStructureMap";
        
        // the VillagerTrades.ITrade implementation classes are package private in the vanilla code
        public static class EmeraldForMapTrade implements VillagerTrades.ITrade {
            private final int emeraldCost;
            private final Supplier<? extends Structure<?>> destination;
            private final MapDecoration.Type destinationIcon;
            private final int customColor;
            private final int maxUses;
            private final int villagerXp;
            private MapTrade destinationType;

            public EmeraldForMapTrade(int pEmeraldCost, Supplier<? extends Structure<?>> pDestination, 
                    MapDecoration.Type pDestinationType, int customColor, int pMaxUses, int pVillagerXp) {
                this.emeraldCost = pEmeraldCost;
                this.destination = pDestination;
                this.destinationIcon = pDestinationType;
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
                        MapData.addTargetDecoration(itemstack, blockpos, "+", this.destinationIcon);
                        if (customColor > 0) {
                            CompoundNBT compoundnbt1 = itemstack.getOrCreateTagElement("display");
                            compoundnbt1.putInt("MapColor", customColor);
                        }
                        itemstack.setHoverName(new TranslationTextComponent("filled_map." + structure.getFeatureName().toLowerCase(Locale.ROOT)));
                        itemstack.getTag().putString("JojoStructure", destinationType.name.toLowerCase()); // no fucking clue why the advancement criteria doesn't work with the custom item name
                        return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(Items.COMPASS), itemstack, this.maxUses, this.villagerXp, 0.2F);
                    } else {
                        return null;
                    }
                }
            }
        }
        
        
        
        public static final Map<VillagerType, VillagerType[]> MOJANG_REBALANCE_MAP_DESTINATION = Util.make(new ImmutableMap.Builder<VillagerType, VillagerType[]>(), mapBuilder -> {
            mapBuilder.put(VillagerType.DESERT, new VillagerType[] {
                                VillagerType.SAVANNA,
                                VillagerType.PLAINS,
                                VillagerType.JUNGLE });
            mapBuilder.put(VillagerType.JUNGLE, new VillagerType[] {
                                VillagerType.SAVANNA,
                                VillagerType.DESERT,
                                VillagerType.SWAMP });
            mapBuilder.put(VillagerType.PLAINS, new VillagerType[] {
                                VillagerType.SAVANNA,
                                VillagerType.TAIGA });
            mapBuilder.put(VillagerType.SAVANNA, new VillagerType[] {
                                VillagerType.DESERT,
                                VillagerType.PLAINS,
                                VillagerType.JUNGLE });
            mapBuilder.put(VillagerType.SNOW, new VillagerType[] {
                                VillagerType.PLAINS,
                                VillagerType.TAIGA,
                                VillagerType.SWAMP });
            mapBuilder.put(VillagerType.SWAMP, new VillagerType[] {
                                VillagerType.SNOW,
                                VillagerType.TAIGA,
                                VillagerType.JUNGLE });
            mapBuilder.put(VillagerType.TAIGA, new VillagerType[] {
                                VillagerType.PLAINS,
                                VillagerType.SNOW,
                                VillagerType.SWAMP });
        }).build();
        
        public static boolean canGiveMap(VillagerType cartographer, VillagerType destination) {
            VillagerType[] possibleDestinations = MOJANG_REBALANCE_MAP_DESTINATION.get(cartographer);
            for (VillagerType type : possibleDestinations) {
                if (destination == type) {
                    return true;
                }
            }
            
            return false;
        }
        
        public static boolean biomeHasOtherStructure(VillagerType biome) {
            return MapTrade.PER_BIOME.containsKey(biome) && !MapTrade.PER_BIOME.get(biome).isEmpty();
        }
    }
    
    
    
    public static boolean addTrade(double randomChance,
            AbstractVillagerEntity trader, ITrade trade, 
            PlayerEntity player, MerchantData merchantData, @Nullable String checkAndSetTag) {
        if (checkAndSetTag != null) {
            if (merchantData.getPlayerTriedTrading(player, checkAndSetTag)) {
                return false;
            }
            merchantData.setPlayerTriedTrading(player, checkAndSetTag);
        }
        
        if (randomChance >= 0 && player.getRandom().nextDouble() < randomChance) {
            MerchantOffer offer = trade.getOffer(trader, trader.getRandom());
            if (offer != null) {
                trader.getOffers().add(offer);
                return true;
            }
        }
        
        return false;
    }
    
}
