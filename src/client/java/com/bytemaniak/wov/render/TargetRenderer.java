package com.bytemaniak.wov.render;

import com.bytemaniak.wov.misc.MiscUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class TargetRenderer implements WorldRenderEvents.End {
    private static final Identifier OVERLAY = Identifier.of("wov:textures/misc/target.png");
    private static final RenderLayer LAYER = RenderLayer.getEntityTranslucentEmissive(OVERLAY);
    private static final VertexConsumerProvider.Immediate vertexConsumerProvider = VertexConsumerProvider.immediate(new BufferAllocator(64));
    private static final double HITSCAN_STEPS = 600;
    private static final double HITSCAN_RADIUS = .05f;

    private double mouseX, mouseY;
    private boolean wasClicked;
    public int focusedEntityId = -1;

    private void genVertex(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Vec3d camera, Vec3d vec, int col, float u, float v) {
        float x = (float)(vec.x - camera.x);
        float y = (float)(vec.y - camera.y);
        float z = (float)(vec.z - camera.z);
        float a = (col >> 24)/255f;
        float r = ((col & 0xFF0000) >> 16)/255f;
        float g = ((col & 0xFF00) >> 8)/255f;
        float b = (col & 0xFF)/255f;
        vertexConsumer.vertex(positionMatrix, x, y, z).color(r, g, b, a).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(1).normal(0, 1, 0);
    }

    private void genQuad(VertexConsumer vertexConsumer, Matrix4f positionMatrix, Vec3d camera, Vec3d pos, double length, int color) {
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(-length, 0, -length), color, 0, 0);
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(-length, 0, length), color, 0, 1);
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(length, 0, length), color, 1, 1);
        genVertex(vertexConsumer, positionMatrix, camera, pos.add(length, 0, -length), color, 1, 0);
    }

    public void setScreenCoords(double x, double y) {
        mouseX = x; mouseY = y;
        wasClicked = true;
    }

    private void getEntityAtMouseCoords(WorldRenderContext context) {
        World world = context.world();
        Camera camera = context.camera();
		int displayHeight = MinecraftClient.getInstance().getWindow().getHeight();
		int displayWidth = MinecraftClient.getInstance().getWindow().getWidth();
		int[] viewport = new int[4];
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
		Vector3f nearF = new Vector3f();
        Vector3f farF = new Vector3f();

		Matrix4f matrixProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f matrixProj2 = new Matrix4f(RenderSystem.getProjectionMatrix());
		Matrix4f matrixModel = new Matrix4f(RenderSystem.getModelViewMatrix());

        matrixProj.mul(matrixModel)
                .mul(context.positionMatrix())
                .unproject((float) mouseX / displayWidth * viewport[2],
                        (float) (displayHeight - mouseY) / displayHeight * viewport[3], 0, viewport, nearF);
        matrixProj2.mul(matrixModel)
                .mul(context.positionMatrix())
                .unproject((float) mouseX / displayWidth * viewport[2],
                        (float) (displayHeight - mouseY) / displayHeight * viewport[3], 1, viewport, farF);

        Vec3d near = new Vec3d(nearF.x, nearF.y, nearF.z).add(camera.getPos());
        Vec3d far = new Vec3d(farF.x, farF.y, farF.z).add(camera.getPos());
        Vec3d step = far.subtract(near).normalize().multiply(.25f);
        Vec3d pos = near;

        for (int i = 0; i < HITSCAN_STEPS; i++) {
            pos = pos.add(step);
            Vec3d minPos = pos.add(new Vec3d(-HITSCAN_RADIUS, -HITSCAN_RADIUS, -HITSCAN_RADIUS));
            Vec3d maxPos = pos.add(new Vec3d(HITSCAN_RADIUS, HITSCAN_RADIUS, HITSCAN_RADIUS));
            Box collisionBox = new Box(minPos, maxPos);
            Vec3i posI = new Vec3i((int)Math.floor(pos.x), (int)Math.floor(pos.y), (int)Math.floor(pos.z));
            BlockPos blockPos = new BlockPos(posI);

            LivingEntity collided = world.getClosestEntity(LivingEntity.class, TargetPredicate.DEFAULT, MinecraftClient.getInstance().player, pos.x, pos.y, pos.z, collisionBox);
            if (collided != null) {
                focusedEntityId = collided.getId();
                break;
            }

            if (world.isChunkLoaded(blockPos)) {
                VoxelShape collisionShape = world.getBlockState(blockPos).getCollisionShape(world, blockPos);
                if (collisionShape != VoxelShapes.empty()) {
                    Box blockCollisionBox = collisionShape.getBoundingBox().offset(blockPos);
                    if (blockCollisionBox.intersects(collisionBox)) {
                        focusedEntityId = -1;
                        break;
                    }
                }
            }
        }

        wasClicked = false;
    }

    @Override
    public void onEnd(WorldRenderContext context) {
        Matrix4f positionMatrix = context.positionMatrix();
        Vec3d cameraPos = context.camera().getPos();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(LAYER);
        LivingEntity focusedEntity;
        Entity entity;

        if (wasClicked) getEntityAtMouseCoords(context);

        if (focusedEntityId == -1) return;
        entity = context.world().getEntityById(focusedEntityId);
        if (!(entity instanceof LivingEntity)) return;
        focusedEntity = (LivingEntity) entity;

        Vec3d pos = focusedEntity.getLerpedPos(context.tickCounter().getTickDelta(true)).add(0, 0.01f, 0);
        double length = focusedEntity.getBoundingBox().getAverageSideLength();
        int color = MiscUtils.getTargetColor(focusedEntity);

        genQuad(vertexConsumer, positionMatrix, cameraPos, pos, length, color);

        vertexConsumerProvider.draw();
    }
}