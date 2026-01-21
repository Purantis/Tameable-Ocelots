package com.purantis.tameableocelots.mixin;

import com.purantis.tameableocelots.TameableOcelotsConfig;
import com.purantis.tameableocelots.util.OcelotDataAccessor;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(OcelotEntity.class)
public abstract class OcelotEntityMixin extends AnimalEntity implements OcelotDataAccessor {

    @Shadow public abstract boolean isTrusting();
    @Shadow public abstract void setTrusting(boolean trusting);
    @Shadow public abstract void showEmoteParticle(boolean positive);

    @Unique
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(OcelotEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    @Unique
    private static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(OcelotEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    protected OcelotEntityMixin(EntityType<? extends AnimalEntity> type, World world) {
        super(type, world);
    }

    @Override
    public boolean isOcelotTrusting() {
        return this.isTrusting();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void initTameData(CallbackInfo ci) {
        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void handleTaming(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);

        // We can now call isTrusting() directly because of the @Shadow
        if (TameableOcelotsConfig.OCELOT_TAMING_ITEMS.contains(itemStack.getItem()) && !this.isTrusting()) {
            if (!this.getWorld().isClient) {
                if (!player.getAbilities().creativeMode) itemStack.decrement(1);

                if (this.random.nextFloat() < TameableOcelotsConfig.config.ocelotsTamingChance) {
                    if (TameableOcelotsConfig.config.convertToCat) {
                        this.convertToCat(player);
                    } else {
                        this.setTrusting(true); // Shadowed
                        this.setOcelotOwnerUuid(player.getUuid());
                        this.showEmoteParticle(true); // Shadowed
                        this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_POSITIVE_PLAYER_REACTION_PARTICLES);
                    }
                } else {
                    this.showEmoteParticle(false); // Shadowed
                    this.getWorld().sendEntityStatus(this, EntityStatuses.ADD_NEGATIVE_PLAYER_REACTION_PARTICLES);
                }
            }
            cir.setReturnValue(ActionResult.success(this.getWorld().isClient));
        }
        else if (this.isTrusting() && player.getUuid().equals(this.getOcelotOwnerUuid())) {
            if (!this.getWorld().isClient) {
                this.setTameOcelotSitting(!this.isTameOcelotSitting());
                this.navigation.stop();
            }
            cir.setReturnValue(ActionResult.success(this.getWorld().isClient));
        }
    }

    @Unique
    private void convertToCat(PlayerEntity player) {
        CatEntity catEntity = EntityType.CAT.create(this.getWorld());
        if (catEntity != null) {
            var registry = this.getWorld().getRegistryManager().get(RegistryKeys.CAT_VARIANT);
            registry.getRandom(this.getWorld().getRandom()).ifPresent(v -> catEntity.setVariant(v.value()));

            catEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
            catEntity.setTamed(true);
            catEntity.setOwner(player);
            this.getWorld().spawnEntity(catEntity);
            this.discard();
        }
    }

    @Inject(method = "isBreedingItem", at = @At("HEAD"), cancellable = true)
    public void customBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        boolean isValid = TameableOcelotsConfig.OCELOT_TAMING_ITEMS.contains(stack.getItem()) ||
                TameableOcelotsConfig.OCELOT_BREEDING_ITEMS.contains(stack.getItem());
        cir.setReturnValue(isValid);
    }

    // --- Accessor Implementation ---
    @Override public boolean isTameOcelotSitting() { return this.dataTracker.get(SITTING); }
    @Override public void setTameOcelotSitting(boolean sitting) { this.dataTracker.set(SITTING, sitting); }
    @Override public UUID getOcelotOwnerUuid() { return this.dataTracker.get(OWNER_UUID).orElse(null); }
    @Override public void setOcelotOwnerUuid(UUID uuid) { this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid)); }

    // --- Persistence (NBT) ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeTameData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("IsSitting", this.isTameOcelotSitting());
        if (this.getOcelotOwnerUuid() != null) nbt.putUuid("Owner", this.getOcelotOwnerUuid());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readTameData(NbtCompound nbt, CallbackInfo ci) {
        this.setTameOcelotSitting(nbt.getBoolean("IsSitting"));
        if (nbt.containsUuid("Owner")) this.setOcelotOwnerUuid(nbt.getUuid("Owner"));
    }
}