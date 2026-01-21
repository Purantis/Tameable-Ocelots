package com.purantis.tameableocelots.goal;

import com.purantis.tameableocelots.util.OcelotDataAccessor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.OcelotEntity;

import java.util.EnumSet;

public class OcelotSitGoal extends Goal {
    private final OcelotEntity ocelot;

    public OcelotSitGoal(OcelotEntity ocelot) {
        this.ocelot = ocelot;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // Cast to Accessor to use the public bridge method
        OcelotDataAccessor accessor = (OcelotDataAccessor) ocelot;

        if (!accessor.isOcelotTrusting()) return false; // Fixed!
        if (ocelot.isSubmergedInWater()) return false;
        return accessor.isTameOcelotSitting();
    }

    @Override
    public void start() {
        ocelot.getNavigation().stop();
    }
}