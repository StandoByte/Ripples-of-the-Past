package com.github.standobyte.jojo.block;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.IStringSerializable;

public class BlockProperties {

    public static final EnumProperty<HorizontalHalf> TABLE_HALF = EnumProperty.create("jojo_table_half", HorizontalHalf.class);
    
    public enum HorizontalHalf implements IStringSerializable {
        LEFT("left"),
        RIGHT("right");
        
        private final String name;
        
        private HorizontalHalf(String pName) {
            this.name = pName;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
        
        @Override
        public String getSerializedName() {
            return this.name;
        }
        
        public HorizontalHalf getOpposite() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }
    
}
