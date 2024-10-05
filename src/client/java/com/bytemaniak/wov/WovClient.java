package com.bytemaniak.wov;

import com.bytemaniak.wov.registry.Keybindings;
import net.fabricmc.api.ClientModInitializer;

public class WovClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Keybindings.registerKeybinds();
	}
}