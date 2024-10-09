package com.bytemaniak.wov.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.option.SimpleOption;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Mutable @Shadow @Final public KeyBinding leftKey;
    @Mutable @Shadow @Final public KeyBinding rightKey;
    @Mutable @Shadow @Final public KeyBinding inventoryKey;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void changeKeybinds(MinecraftClient client, File optionsFile, CallbackInfo ci) {
        leftKey = new KeyBinding("key.left", GLFW.GLFW_KEY_Q, KeyBinding.MOVEMENT_CATEGORY);
        rightKey = new KeyBinding("key.right", GLFW.GLFW_KEY_E, KeyBinding.MOVEMENT_CATEGORY);
        inventoryKey = new KeyBinding("key.inventory", GLFW.GLFW_KEY_B, KeyBinding.INVENTORY_CATEGORY);
    }

    @ModifyReturnValue(method = "getPerspective", at = @At("RETURN"))
    private Perspective forceThirdPerson(Perspective original) {
        return Perspective.THIRD_PERSON_BACK;
    }

    @ModifyReturnValue(method = "getBobView", at = @At("RETURN"))
    private SimpleOption<Boolean> cancelBobbing(SimpleOption<Boolean> original) {
        return SimpleOption.ofBoolean("options.viewBobbing", false);
    }
}
