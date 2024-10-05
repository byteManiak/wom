package com.bytemaniak.wov.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameMenuScreen.class)
public class GameMenuScreenMixin {
    @WrapOperation(method = "method_19845", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Mouse;lockCursor()V"))
    private void noLockCursor(Mouse mouse, Operation<Void> original) {}
}
