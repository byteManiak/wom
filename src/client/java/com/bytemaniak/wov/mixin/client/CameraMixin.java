package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.interfaces.WowCamera;
import com.bytemaniak.wov.registry.Keybindings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "update", at = @At("HEAD"))
    private void applyTurn(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!MinecraftClient.getInstance().mouse.wasRightButtonClicked()) {
            if (Keybindings.TURN_LEFT.isPressed())
                focusedEntity.setYaw(focusedEntity.getYaw() - 3.5f*tickDelta);
            if (Keybindings.TURN_RIGHT.isPressed())
                focusedEntity.setYaw(focusedEntity.getYaw() + 3.5f*tickDelta);
        }
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