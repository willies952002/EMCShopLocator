/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 *
 * @author gbl
 */
public class ShopSearchGuiHandler implements IGuiHandler {
    
    public static final int ShopSearchGui=0;
    private static ShopSearchGui guiInstance;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID==ShopSearchGui) {
            if (guiInstance==null) {
                // System.out.println("new gui!");
                guiInstance=new ShopSearchGui();
            }
            // System.out.println("return :"+guiInstance);
            return guiInstance;
        }
        return null;
    }
}
