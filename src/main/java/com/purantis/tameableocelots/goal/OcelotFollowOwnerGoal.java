package com.purantis.tameableocelots.goal;

import com.purantis.tameableocelots.util.OcelotDataAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.OcelotEntity;

import java.util.EnumSet;
import java.util.UUID;

public class OcelotFollowOwnerGoal extends Goal {
    private final OcelotEntity ocelot;
    private final OcelotDataAccessor accessor;
    private LivingEntity owner;
    private final double speed;
    private final float minDistance;
    private final float maxDistance;

    public OcelotFollowOwnerGoal(OcelotEntity ocelot, double speed, float minDistance, float maxDistance) {
        this.ocelot = ocelot;
        this.accessor = (OcelotDataAccessor) ocelot;
        this.speed = speed;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        if (!accessor.isOcelotTrusting()) return false;
        if (accessor.isTameOcelotSitting()) return false;

        UUID uuid = accessor.getOcelotOwnerUuid();
        if (uuid == null) return false;

        this.owner = ocelot.getWorld().getPlayerByUuid(uuid);
        return this.owner != null && ocelot.squaredDistanceTo(this.owner) > (minDistance * minDistance);
    }

    @Override
    public void tick() {
        ocelot.getLookControl().lookAt(this.owner, 10.0F, (float)ocelot.getMaxLookPitchChange());
        ocelot.getNavigation().startMovingTo(this.owner, this.speed);
    }

    @Override
    public boolean shouldContinue() {
        return !accessor.isTameOcelotSitting() && ocelot.squaredDistanceTo(this.owner) > (maxDistance * maxDistance);
    }
}