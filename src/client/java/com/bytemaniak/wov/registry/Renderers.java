package com.bytemaniak.wov.registry;

import com.bytemaniak.wov.render.TargetRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class Renderers {
    public static TargetRenderer TARGET_RENDERER = new TargetRenderer();

    public static void initRenderers() {
        WorldRenderEvents.END.register(TARGET_RENDERER);
    }
}
