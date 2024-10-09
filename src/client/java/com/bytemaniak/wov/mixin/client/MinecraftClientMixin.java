package com.bytemaniak.wov.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow @Final public GameOptions options;

    @WrapOperation(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setPerspective(Lnet/minecraft/client/option/Perspective;)V"))
    private void forceThirdPerson(GameOptions instance, Perspective perspective, Operation<Void> original) {
        original.call(instance, Perspective.THIRD_PERSON_BACK);
    }

    @WrapOperation(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;lockCursor()V"))
    private void noLockCursor(Mouse mouse, Operation<Void> original) {}

    @WrapOperation(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;wasPressed()Z"))
    private boolean preventVanillaActions(KeyBinding key, Operation<Boolean> original) {
        if (key.equals(options.attackKey) || key.equals(options.useKey) || key.equals(options.pickItemKey))
            return false;

        return original.call(key);
    }

    @WrapOperation(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean preventVanillaActions2(KeyBinding key, Operation<Boolean> original) {
        if (key.equals(options.attackKey) || key.equals(options.useKey))
            return false;

        return original.call(key);
    }
}
