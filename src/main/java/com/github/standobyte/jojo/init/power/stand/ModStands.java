package com.github.standobyte.jojo.init.power.stand;

import com.github.standobyte.jojo.entity.stand.StandEntityType;
import com.github.standobyte.jojo.entity.stand.stands.CrazyDiamondEntity;
import com.github.standobyte.jojo.entity.stand.stands.GoldExperienceEntity;
import com.github.standobyte.jojo.entity.stand.stands.HierophantGreenEntity;
import com.github.standobyte.jojo.entity.stand.stands.MagiciansRedEntity;
import com.github.standobyte.jojo.entity.stand.stands.SilverChariotEntity;
import com.github.standobyte.jojo.entity.stand.stands.StarPlatinumEntity;
import com.github.standobyte.jojo.entity.stand.stands.TheWorldEntity;
import com.github.standobyte.jojo.init.power.stand.EntityStandRegistryObject.EntityStandSupplier;
import com.github.standobyte.jojo.power.impl.stand.stats.ArmoredStandStats;
import com.github.standobyte.jojo.power.impl.stand.stats.StandStats;
import com.github.standobyte.jojo.power.impl.stand.stats.TimeStopperStandStats;
import com.github.standobyte.jojo.power.impl.stand.type.EntityStandType;

/* 
 * that's just for the sake of distinguishing between "stand types" and "stand objects" when writing code
 * while keeping the initialization of stand types and related stand actions in the same file
 * ("ModStands.STAR_PLATINUM" looks nicer, but stuff like 
 * "STAR_PLATINUM_HEAVY_PUNCH" interfering with the auto-complete makes it less convenient)
 * so it's totally optional
 */
public class ModStands {

    public static final EntityStandSupplier<EntityStandType<TimeStopperStandStats>, StandEntityType<StarPlatinumEntity>> 
    STAR_PLATINUM = new EntityStandSupplier<>(ModStandsInit.STAND_STAR_PLATINUM);

    public static final EntityStandSupplier<EntityStandType<TimeStopperStandStats>, StandEntityType<TheWorldEntity>> 
    THE_WORLD = new EntityStandSupplier<>(ModStandsInit.STAND_THE_WORLD);

    public static final EntityStandSupplier<EntityStandType<StandStats>, StandEntityType<HierophantGreenEntity>> 
    HIEROPHANT_GREEN = new EntityStandSupplier<>(ModStandsInit.STAND_HIEROPHANT_GREEN);

    public static final EntityStandSupplier<EntityStandType<ArmoredStandStats>, StandEntityType<SilverChariotEntity>> 
    SILVER_CHARIOT = new EntityStandSupplier<>(ModStandsInit.STAND_SILVER_CHARIOT);

    public static final EntityStandSupplier<EntityStandType<StandStats>, StandEntityType<MagiciansRedEntity>> 
    MAGICIANS_RED = new EntityStandSupplier<>(ModStandsInit.STAND_MAGICIANS_RED);

    public static final EntityStandSupplier<EntityStandType<StandStats>, StandEntityType<CrazyDiamondEntity>> 
    CRAZY_DIAMOND = new EntityStandSupplier<>(ModStandsInit.STAND_CRAZY_DIAMOND);

    public static final EntityStandSupplier<EntityStandType<StandStats>, StandEntityType<GoldExperienceEntity>> 
    GOLD_EXPERIENCE = new EntityStandSupplier<>(ModStandsInit.STAND_GOLD_EXPERIENCE);
}
