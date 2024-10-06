package com.bytemaniak.wov.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetRenderer implements WorldRenderEvents.End {
    private static final Identifier OVERLAY = Identifier.of("wov:textures/misc/target.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityTranslucentEmissive(OVERLAY);
    private static final VertexConsumerProvider.Immediate vertexConsumerProvider = VertexConsumerProvider.immediate(new BufferAllocator(64));

    private void genVertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Vec3d camera, Vec3d vec, Vec3d col, float u, float v) {
        float x = (float)(vec.x - camera.x);
        float y = (float)(vec.y - camera.y);
        float z = (float)(vec.z - camera.z);
        vertexConsumer.vertex(positionMatrix, x, y, z).color((float)col.x, (float)col.y, (float)col.z, 1).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(1).normal(0, 1, 0);
    }

    private void genQuad(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Vec3d camera, Vec3d pos, double length, Vec3d color) {
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(-length, 0, -length), color, 0, 0);
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(-length, 0, length), color, 0, 1);
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(length, 0, length), color, 1, 1);
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(length, 0, -length), color, 1, 0);
    }

    @Override
    public void onEnd(WorldRenderContext context) {
        Matrix4f positionMatrix = context.positionMatrix();
        Vec3d cameraPos = context.camera().getPos();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
        /// TODO: Get the actual focused entity
        LivingEntity focusedEntity = MinecraftClient.getInstance().player;

        if (focusedEntity == null) return;

        Vec3d pos = focusedEntity.getLerpedPos(context.tickCounter().getTickDelta(true)).add(0, 0.01f, 0);
        double length = focusedEntity.getBoundingBox().getAverageSideLength();
        Vec3d color = focusedEntity instanceof HostileEntity ? new Vec3d(1, 0, 0) :
                focusedEntity instanceof PassiveEntity ? new Vec3d(1, 1, 0) :
                focusedEntity.isDead() ? new Vec3d(.25, .25, .25) : new Vec3d(0, 1, 0);

        genQuad(vertexConsumer, positionMatrix, cameraPos, pos, length, color);

        vertexConsumerProvider.draw();
    }
}