package com.bytemaniak.wov.registry;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public static KeyBinding TURN_LEFT, TURN_RIGHT;

    private static KeyBinding registerKeybind(String id, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wov."+id, InputUtil.Type.KEYSYM, key,
                "category.wov.bindings"
        ));
    }

    public static void registerKeybinds() {
        TURN_LEFT = registerKeybind("turn_left", GLFW.GLFW_KEY_A);
        TURN_RIGHT = registerKeybind("turn_right", GLFW.GLFW_KEY_D);
    }
}
