package com.github.standobyte.jojo.client.playeranim.playeranimator;

import dev.kosmx.playerAnim.impl.IBendHelper;
import dev.kosmx.playerAnim.impl.animation.BendHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Direction;

public class BendyLibHelper {

    static float[] getBend(IBendHelper bendablePart) {
        return BendDummy.DUMMY.getBend(bendablePart);
    }
    
    // i hate it
    private static class BendDummy extends BendHelper {
        private static final BendDummy DUMMY = new BendDummy();

        private BendDummy() {
            super(new ModelRenderer(0, 0, 0, 0), false, null);
            addBendedCuboid(0, 0, 0, 0, 0, 0, 0, Direction.UP);
        }
        
        private float[] getBend(IBendHelper bendablePart) {
            copyBend(bendablePart);
            return new float[] { angl, axis };
        }
        
    }
}
