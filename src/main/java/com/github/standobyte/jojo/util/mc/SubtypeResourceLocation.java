package com.github.standobyte.jojo.util.mc;

import java.util.Locale;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.util.ResourceLocation;

public class SubtypeResourceLocation extends ResourceLocation {
    @Nullable private final String variant;

    protected SubtypeResourceLocation(String[] elements) {
        super(elements);
        this.variant = StringUtils.isEmpty(elements[2]) ? null : elements[2].toLowerCase(Locale.ROOT);
    }

    public SubtypeResourceLocation(String str) {
        this(decompose(str));
    }

    public SubtypeResourceLocation(ResourceLocation id, @Nullable String variant) {
        this(new String[] { id.getNamespace(), id.getPath(), variant } );
    }

    public SubtypeResourceLocation(String namespace, String path, String variant) {
        this(new String[] { namespace, path, variant } );
    }

    public SubtypeResourceLocation(String namespace, String path) {
        this(new String[] { namespace, path, null } );
    }

    protected static String[] decompose(String str) {
        String[] elements = new String[]{null, str, null};
        int variantDelimPos = str.indexOf('#');
        String s = str;
        if (variantDelimPos >= 0) {
            elements[2] = str.substring(variantDelimPos + 1, str.length());
            if (variantDelimPos > 1) {
                s = str.substring(0, variantDelimPos);
            }
        }

        System.arraycopy(ResourceLocation.decompose(s, ':'), 0, elements, 0, 2);
        return elements;
    }

    @Nullable
    public String getVariant() {
        return this.variant;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj.getClass() == ResourceLocation.class) {
            return this.variant == null && super.equals(obj);
        } else if (obj instanceof SubtypeResourceLocation && super.equals(obj)) {
            SubtypeResourceLocation other = (SubtypeResourceLocation) obj;
            return this.variant == other.variant || this.variant != null && this.variant.equals(other.variant);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (this.variant == null) {
            return super.hashCode();
        }
        return 31 * super.hashCode() + this.variant.hashCode();
    }

    @Override
    public String toString() {
        if (this.variant == null) {
            return super.toString();
        }
        return super.toString() + '#' + this.variant;
    }
    
    

}
