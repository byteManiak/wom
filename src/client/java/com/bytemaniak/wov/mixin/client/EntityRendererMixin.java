package com.bytemaniak.wov.mixin.client;

import com.bytemaniak.wov.misc.MiscUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Shadow @Final protected EntityRenderDispatcher dispatcher;

    @Shadow public abstract TextRenderer getTextRenderer();

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;renderLabelIfPresent(Lnet/minecraft/entity/Entity;Lnet/minecraft/text/Text;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IF)V"))
    private void s(EntityRenderer<T> renderer, T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, float tickDelta, Operation<Void> original) {
        if (entity instanceof MobEntity mob) {
            double d = dispatcher.getSquaredDistanceToCamera(entity);
            if (!(d > 4096.0)) {
                Vec3d v = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getYaw(tickDelta));
                if (v != null) {
                    matrices.push();
                    matrices.translate(v.x, v.y+.5, v.z);
                    matrices.multiply(dispatcher.getRotation());
                    matrices.scale(.025f, -.025f, .025f);
                    Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                    TextRenderer textRenderer = getTextRenderer();
                    float x = -textRenderer.getWidth(text) / 2f;
                    float opacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(.25f);
				    int background = (int)(opacity * 255) << 24;
                    int color = MiscUtils.getTargetColor(mob);

                    textRenderer.draw(text, x, 0, 0, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.SEE_THROUGH, background, light);
                    textRenderer.draw(text, x, 0, color, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);

                    matrices.pop();
                }
            }
        } else original.call(renderer, entity, text, matrices, vertexConsumers, light, tickDelta);
    }
}
