package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.interfaces.WowCamera;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin implements WowCamera {
    @Unique private float offsetYaw = 0, offsetPitch = 0;
    @Unique private float offsetZoom = 0;

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getYaw(F)F"))
    private float applyYawOffset(Entity entity, float tickDelta, Operation<Float> original) {
        return original.call(entity, tickDelta) + offsetYaw;
    }

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPitch(F)F"))
    private float applyPitchOffset(Entity entity, float tickDelta, Operation<Float> original) {
        return offsetPitch;
    }

    @Override
    public void wov$applyRotation(double dx, double dy, boolean reset) {
        float fdx = (float)dx * .15f;
        float fdy = (float)dy * .15f;
        offsetYaw = reset ? fdx : (offsetYaw + fdx);
        offsetPitch = Math.clamp(offsetPitch + fdy, -90, 90);
    }

    @Override
    public float wov$getOffsetYaw() { return offsetYaw; }

    @Override
    public void wov$applyZoom(double amount) {
        offsetZoom += (float)amount/2;
        offsetZoom = Math.clamp(offsetZoom, -6, 4);
    }

    @WrapOperation(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(F)F"))
    private float applyZoomOffset(Camera camera, float f, Operation<Float> original) {
        return original.call(camera, f-offsetZoom);
    }
}