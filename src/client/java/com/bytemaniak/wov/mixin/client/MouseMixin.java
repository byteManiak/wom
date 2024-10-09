package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.interfaces.WowCamera;
import com.bytemaniak.wov.registry.Keybindings;
import com.bytemaniak.wov.registry.Renderers;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.entity.player.PlayerInventory;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Shadow private double x;
    @Shadow private double y;

    @Shadow public abstract void unlockCursor();
    @Shadow public abstract boolean isCursorLocked();

    @Unique private double lastX, lastY;
    @Unique private double leftClickStartTick = 0, rightClickStartTick = 0;
    @Unique private static final double CLICK_ACTION_TIME = .15f;

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void handleMouseClick(long window, int button, int action, int mods, CallbackInfo ci) {
        if (action == 1 && !isCursorLocked()) {
            lastX = x; lastY = y;

            if (button == 0)
                leftClickStartTick = GlfwUtil.getTime();
            if (button == 1)
                rightClickStartTick = GlfwUtil.getTime();
        }
        // Unlock cursor only if a single button was pressed
        else if (action == 0 &&
            (leftButtonClicked ^ rightButtonClicked ^ middleButtonClicked)) {
            unlockCursor();
            x = lastX; y = lastY;
            GLFW.glfwSetCursorPos(client.getWindow().getHandle(), x, y);

            if (button == 0) {
                double leftClickEndTick = GlfwUtil.getTime();
                if (leftClickEndTick - leftClickStartTick < CLICK_ACTION_TIME)
                    Renderers.TARGET_RENDERER.setScreenCoords(x, y, false);
            }
            if (button == 1) {
                double rightClickEndTick = GlfwUtil.getTime();
                if (rightClickEndTick - rightClickStartTick < CLICK_ACTION_TIME)
                    Renderers.TARGET_RENDERER.setScreenCoords(x, y, true);
            }
        }
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
