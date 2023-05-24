package com.github.standobyte.jojo.power.impl.stand.stats;

import net.minecraft.network.PacketBuffer;

public class ArmoredStandStats extends StandStats {
    public final double armor;
    public final double armorToughness;
    
    protected ArmoredStandStats(Builder builder) {
        super(builder);
        this.armor = builder.armor;
        this.armorToughness = builder.armorToughness;
    }
    
    protected ArmoredStandStats(PacketBuffer buf) {
        super(buf);
        this.armor = buf.readDouble();
        this.armorToughness = buf.readDouble();
    }
    
    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeDouble(armor);
        buf.writeDouble(armorToughness);
    }
    
    @Override
    public double getArmor() {
        return armor;
    }
    
    @Override
    public double getArmorToughness() {
        return armorToughness;
    }
    
    static {
        registerFactory(ArmoredStandStats.class, ArmoredStandStats::new);
    }
    
    

    public static class Builder extends AbstractBuilder<Builder, ArmoredStandStats> {
        private double armor = 0;
        private double armorToughness = 0;
        
        public Builder armor(double armor) {
            this.armor = armor;
            return getThis();
        }
        
        public Builder armorToughness(double armorToughness) {
            this.armorToughness = armorToughness;
            return getThis();
        }
        
        @Override
        protected Builder getThis() {
            return this;
        }
        
        @Override
        protected ArmoredStandStats createStats() {
            return new ArmoredStandStats(this);
        }
    }
}
