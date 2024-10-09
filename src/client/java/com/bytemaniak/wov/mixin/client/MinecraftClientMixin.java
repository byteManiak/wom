package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.registry.Renderers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final public GameOptions options;
    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;
    @Shadow @Nullable public ClientPlayerEntity player;
    @Shadow @Nullable public ClientWorld world;

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

    @WrapOperation(method = "handleInputEvents", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I"))
    private void attackOnHotbarPress(PlayerInventory instance, int value, Operation<Void> original) {
        original.call(instance, value);
        Entity focusedEntity = world.getEntityById(Renderers.TARGET_RENDERER.focusedEntityId);
        if (focusedEntity instanceof LivingEntity) {
            interactionManager.attackEntity(player, focusedEntity);
            player.swingHand(Hand.MAIN_HAND);
        }
    }
}
