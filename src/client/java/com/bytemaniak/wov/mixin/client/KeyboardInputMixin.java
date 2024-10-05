package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.registry.Keybindings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean moveWithMouse(KeyBinding binding, Operation<Boolean> original) {
        MinecraftClient instance = MinecraftClient.getInstance();
        Mouse mouse = instance.mouse;
        if (binding.equals(instance.options.forwardKey)) {
            if (mouse.wasLeftButtonClicked() && mouse.wasRightButtonClicked()) return true;
        } else if (binding.equals(instance.options.leftKey)) {
            if (mouse.wasRightButtonClicked() && Keybindings.TURN_LEFT.isPressed()) return true;
        } else if (binding.equals(instance.options.rightKey)) {
            if (mouse.wasRightButtonClicked() && Keybindings.TURN_RIGHT.isPressed()) return true;
        }
        return original.call(binding);
    }
}
