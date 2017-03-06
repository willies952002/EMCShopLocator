package de.guntram.mcmod.emcshoplocator.gui;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class ShopSearchKeyHandler {
    
    @SideOnly(Side.CLIENT)
    public static KeyBinding showGui;
    
    public static void init() {
        ClientRegistry.registerKeyBinding(showGui = new KeyBinding("key.shopsearch", Keyboard.KEY_APOSTROPHE, "key.categories.emcshoplocator"));
    }
}
