package com.bytemaniak.wov.misc;

import com.bytemaniak.wov.interfaces.AngryAt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;
import java.util.UUID;

public class MiscUtils {
    public static int getTargetColor(LivingEntity entity) {
        int color = 0xFF00FF00;
        if (entity.isDead()) color = 0xFF888888;
        else if (entity instanceof PlayerEntity) color = 0xFFFFFF00;
        else if (entity instanceof Monster) color = 0xFFFF0000;
        else if (entity instanceof Angerable angerable) {
            UUID playerUuid = MinecraftClient.getInstance().player.getUuid();
            Optional<UUID> angryAt = ((AngryAt)angerable).wov$getAngryAt();
            if (angryAt.isEmpty()) color = 0xFFFFFF00;
            else if (angryAt.get().equals(playerUuid)) color = 0xFFFF0000;
        }
        return color;
    }
}
