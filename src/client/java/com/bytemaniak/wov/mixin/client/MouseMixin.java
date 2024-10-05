package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.interfaces.WowCamera;
import com.bytemaniak.wov.registry.Keybindings;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private boolean rightButtonClicked;
    @Shadow private boolean leftButtonClicked;
    @Shadow private boolean middleButtonClicked;
    @Shadow private double lastTickTime;

    @Shadow public abstract void unlockCursor();

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void unlockCursorOnRelease(long window, int button, int action, int mods, CallbackInfo ci) {
        // Unlock cursor only if a single button was pressed
        if (action == 0 &&
            (leftButtonClicked ^ rightButtonClicked ^ middleButtonClicked))
            unlockCursor();
    }

    @WrapWithCondition(method = "updateMouse" , at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    private boolean changeLookDirection(ClientPlayerEntity player, double dx, double dy) {
        WowCamera camera = (WowCamera) client.gameRenderer.getCamera();
        camera.wov$applyRotation(dx, dy, false);
        if (rightButtonClicked) {
            player.setYaw(player.getYaw()+camera.wov$getOffsetYaw());
            camera.wov$applyRotation(0, 0, true);
        }

        return false;
    }

    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    private boolean applyCameraZoom(PlayerInventory inventory, double scrollAmount) {
        WowCamera camera = (WowCamera) client.gameRenderer.getCamera();
        camera.wov$applyZoom(scrollAmount);

        return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void applyTurn(CallbackInfo ci) {
        float timeDelta = (float)(GlfwUtil.getTime() - lastTickTime);

        if (!rightButtonClicked) {
            ClientPlayerEntity player = client.player;
            if (player == null) return;

            if (Keybindings.TURN_LEFT.isPressed())
                player.setYaw(player.getYaw() - 150*timeDelta);
            if (Keybindings.TURN_RIGHT.isPressed())
                player.setYaw(player.getYaw() + 150*timeDelta);
        }
    }
}
