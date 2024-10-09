package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.interfaces.AngryAt;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Optional;
import java.util.UUID;

@Mixin(PathAwareEntity.class)
public abstract class PathAwareEntityMixin extends MobEntity implements AngryAt {
    @Unique private static final TrackedData<Optional<UUID>> ANGRY_AT =
            DataTracker.registerData(PathAwareEntityMixin.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

    protected PathAwareEntityMixin(EntityType<? extends MobEntity> entityType, World world) { super(entityType, world); }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANGRY_AT, Optional.empty());
    }

    @Override
    public TrackedData<Optional<UUID>> wov$getTrackedData() { return ANGRY_AT; }

    @Override
    public Optional<UUID> wov$getAngryAt() { return dataTracker.get(ANGRY_AT); }
}
