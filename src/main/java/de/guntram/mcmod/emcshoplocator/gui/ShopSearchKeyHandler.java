package de.guntram.mcmod.emcshoplocator.gui;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ShopSearchKeyHandler {
    @SubscribeEvent
    public void keyPressed(final InputEvent.KeyInputEvent e) {
        if (ShopSearchKeyRegistration.showGui.isPressed())
            Minecraft.getMinecraft().player.openGui(EMCShopLocator.instance, ShopSearchGuiHandler.ShopSearchGui, Minecraft.getMinecraft().world, 0, 0, 0);
    }
}
