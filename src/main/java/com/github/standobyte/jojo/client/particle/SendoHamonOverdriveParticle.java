package com.github.standobyte.jojo.client.particle;

import net.minecraft.client.world.ClientWorld;

public class SendoHamonOverdriveParticle extends HamonSparkParticle {

    public SendoHamonOverdriveParticle(ClientWorld world, double x, double y, double z, 
            double xDDDDD, double yd, double zd) {
        super(world, x, y, z, xDDDDD, yd, zd);
        this.xd = xDDDDD;
        this.yd = yd;
        this.zd = zd;
    }
    
    @Override
    public void tick() {
       this.xo = this.x;
       this.yo = this.y;
       this.zo = this.z;
       if (this.age++ >= this.lifetime) {
          this.remove();
       } else {
          this.move(this.xd, this.yd, this.zd);
       }
    }
}
