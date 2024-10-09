package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.interfaces.AngryAt;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.UUID;

@Mixin(Angerable.class)
public interface AngerableMixin {
    @Unique private void setAngryAtTracked(Angerable angerable, UUID uuid) {
        if (angerable instanceof Angerable entity) {
            AngryAt angryAt = (AngryAt)entity;
            DataTracker dataTracker = ((PathAwareEntity)entity).getDataTracker();
            if (uuid == null) dataTracker.set(angryAt.wov$getTrackedData(), Optional.empty());
            else dataTracker.set(angryAt.wov$getTrackedData(), Optional.of(uuid));
        }
    }

    @WrapOperation(method = "tickAngerLogic", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/Angerable;setAngryAt(Ljava/util/UUID;)V"))
    private void tickSetAngryAt(Angerable angerable, UUID uuid, Operation<Void> original) {
        original.call(angerable, uuid);
        setAngryAtTracked(angerable, uuid);
    }

    @WrapOperation(method = "readAngerFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/Angerable;setAngryAt(Ljava/util/UUID;)V"))
    private void readNbtSetAngryAt(Angerable angerable, UUID uuid, Operation<Void> original) {
        original.call(angerable, uuid);
        setAngryAtTracked(angerable, uuid);
    }

    @WrapOperation(method = "stopAnger", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/Angerable;setAngryAt(Ljava/util/UUID;)V"))
    private void stopSetAngryAt(Angerable angerable, UUID uuid, Operation<Void> original) {
        original.call(angerable, uuid);
        setAngryAtTracked(angerable, uuid);
    }
}
